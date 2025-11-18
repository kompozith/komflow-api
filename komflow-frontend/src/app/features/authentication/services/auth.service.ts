import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { AuthUser } from '../models/auth-user';
import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { PasswordResetVerifyResponse } from '../models/password-reset-verify-response';
import { PasswordResetCompleteResponse } from '../models/password-reset-complete-response';
import { AUTH_API, AUTH_CONFIG } from '../auth.constants';
import { Router } from '@angular/router';
import { PermissionService } from '../../../services/permission.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_BASE_URL = `${environment.apiUrl}/auth`;

  private currentUserSubject = new BehaviorSubject<AuthUser | null>(this.getSavedUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  // Reference to any active auto-logout timer so we can clear/reset it when tokens update
  private logoutTimer: any | null = null;

  constructor(private http: HttpClient, private router: Router, private permissionService: PermissionService) {
    console.debug('AuthService: constructor - initializing service');
    // Check token expiration on service initialization
    this.checkTokenExpiration();
    console.debug('AuthService: constructor - checkTokenExpiration completed');
  }

  private getDefaultHeaders(): { [key: string]: string } {
    return {
      'Content-Type': 'application/json',
      'accept': '*/*'
    };
  }

  /**
   * Login user with email/phone and password
   */
  login(credentials: LoginRequest, rememberMe: boolean = false): Observable<LoginResponse> {
    const loginData = {
      login: credentials.login,
      password: credentials.password
    };

    return this.http.post<LoginResponse>(`${this.API_BASE_URL}/login`, loginData, { headers: this.getDefaultHeaders() }).pipe(
      tap(response => {
        this.setSession(response, rememberMe);
        // Load permissions after successful login
        this.loadUserPermissions().subscribe();
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Logout user and clear session.
   *
   * Sends a POST request to the backend to invalidate the server-side session/token.
   * Regardless of the HTTP result (success or failure) the client-side session is cleared
   * to ensure the user is logged out locally.
   */
  logout(): void {
    // Call backend logout endpoint. Use subscribe so callers don't need to change (keeps void signature).
    this.http.post(`${this.API_BASE_URL}/logout`, {}, { headers: this.getDefaultHeaders() }).pipe(
      tap(() => {
        // Successfully logged out on server (no-op here)
      }),
      catchError(err => {
        // Log the error but continue to clear client session
        console.error('Logout request failed:', err);
        // Return an observable so the stream completes and subscribe error handler runs.
        return throwError(() => err);
      })
    ).subscribe({
      next: () => {
        this.clearSession();
        this.currentUserSubject.next(null);
        this.isAuthenticatedSubject.next(false);
        // Clear permissions on logout
        this.clearPermissions();
        this.router.navigate(['/authentication/login']);
      },
      error: () => {
        // Even if the server call failed, ensure client session is cleared
        this.clearSession();
        this.currentUserSubject.next(null);
        this.isAuthenticatedSubject.next(false);
        // Clear permissions on logout
        this.clearPermissions();
      }
    });
  }

  /**
   * Check if user is currently authenticated
   */
  isAuthenticated(): boolean {
    const isAuth = this.isAuthenticatedSubject.value;
    console.debug('AuthService.isAuthenticated: returning', isAuth);
    return isAuth;
  }

  /**
   * Get current authenticated user
   */
  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get stored authentication token
   */
  getToken(): string | null {
    return this.getStorageItem(AUTH_CONFIG.TOKEN_KEY);
  }

  /**
   * Refresh token using refresh token in Authorization header
   */
  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getStorageItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);

    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No refresh token available'));
    }

    // Send refresh token in Authorization header as per API docs
    const headers = {
      ...this.getDefaultHeaders(),
      'Authorization': `Bearer ${refreshToken}`
    };

    return this.http.post<LoginResponse>(
      `${this.API_BASE_URL}/refresh`,
      {}, // Empty body since token is in header
      { headers }
    ).pipe(
      tap(response => {
        this.updateAccessToken(response.accessToken, response.expiresIn);
      }),
      catchError(err => {
        this.logout();
        return throwError(() => new Error('Session expired. Please log in again.'));
      })
    );
  }

  /**
   * Initiate password reset (Step 1)
   * - Checks whether the contact exists and triggers backend to send OTP.
   * - contactType expected values: 'PHONE' | 'EMAIL' (match backend enum).
   */
  initiatePasswordReset(contact: string, contactType: 'PHONE' | 'EMAIL' = 'PHONE'): Observable<any> {
    const body = { contact, contactType };
    return this.http.post<any>(`${this.API_BASE_URL}/password-reset/initiate`, body, { headers: this.getDefaultHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Verify password reset OTP (Step 2)
   * - Returns a reset token on successful verification.
   */
  verifyPasswordReset(contact: string, otpCode: string): Observable<PasswordResetVerifyResponse> {
    const body = { contact, otpCode };
    return this.http.post<PasswordResetVerifyResponse>(`${this.API_BASE_URL}/password-reset/verify`, body, { headers: this.getDefaultHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Complete password reset (Step 3)
   * - Consume the resetToken and set the new password.
   * - On success the user can then login with the new password.
   */
  completePasswordReset(resetToken: string, newPassword: string): Observable<PasswordResetCompleteResponse> {
    const body = { resetToken, newPassword };
    return this.http.post<PasswordResetCompleteResponse>(`${this.API_BASE_URL}/password-reset/complete`, body, { headers: this.getDefaultHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  private updateAccessToken(token: string, expiresIn: number): void {
    const rememberMe = this.getStorageItem(AUTH_CONFIG.REMEMBER_KEY) === 'true';
    const storage = rememberMe ? localStorage : sessionStorage;

    storage.setItem(AUTH_CONFIG.TOKEN_KEY, token);
    // Protect against invalid/missing expiresIn coming from backend - fallback to 1 hour
    const safeExpires = Number.isFinite(Number(expiresIn)) && Number(expiresIn) > 0 ? Number(expiresIn) : 3600;
    const expirationTime = Date.now() + safeExpires * 1000;
    storage.setItem(`${AUTH_CONFIG.TOKEN_KEY_EXPIRES}`, expirationTime.toString());

    console.debug('AuthService.updateAccessToken: expiresIn from backend:', expiresIn, 'safeExpires:', safeExpires, 'expirationTime:', new Date(expirationTime));

    this.isAuthenticatedSubject.next(true);

    // Reset auto logout timer using the safe value
    this.setupAutoLogout(safeExpires);
  }

  /**
   * Private methods
   */
  private setSession(response: LoginResponse, rememberMe: boolean): void {
    const storage = rememberMe ? localStorage : sessionStorage;

    // Tokens
    storage.setItem(AUTH_CONFIG.TOKEN_KEY, response.accessToken);
    storage.setItem(AUTH_CONFIG.REFRESH_TOKEN_KEY, response.refreshToken);

    // User - map backend user to AuthUser format
    const authUser: AuthUser = {
      id: response.user.username, // Use username as ID for now
      firstName: response.user.firstName,
      lastName: response.user.lastName,
      email: response.user.email,
      phone: response.user.phoneNumbers?.[0]?.number, // Take first phone number if available
      role: 'USER', // Default role, could be enhanced later
      accountStatus: 'ACTIVE', // Default status
      profilePicture: undefined
    };

    storage.setItem(AUTH_CONFIG.USER_KEY, JSON.stringify(authUser));

    // Remember
    storage.setItem(AUTH_CONFIG.REMEMBER_KEY, rememberMe.toString());

    // Expiration - guard against missing/invalid expiresIn
    const safeExpires = Number.isFinite(Number(response.expiresIn)) && Number(response.expiresIn) > 0 ? Number(response.expiresIn) : 3600;
    const expirationTime = Date.now() + safeExpires * 1000;
    storage.setItem(`${AUTH_CONFIG.TOKEN_KEY_EXPIRES}`, expirationTime.toString());

    console.debug('AuthService.setSession: expiresIn from backend:', response.expiresIn, 'safeExpires:', safeExpires, 'expirationTime:', new Date(expirationTime));

    this.currentUserSubject.next(authUser);
    this.isAuthenticatedSubject.next(true);

    this.setupAutoLogout(safeExpires);
  }

  private clearSession(): void {
    // Debug current storage values before clearing for easier tracing
    try {
      console.debug('AuthService.clearSession: before clear', {
        local: {
          token: localStorage.getItem(AUTH_CONFIG.TOKEN_KEY),
          refresh: localStorage.getItem(AUTH_CONFIG.REFRESH_TOKEN_KEY),
          user: localStorage.getItem(AUTH_CONFIG.USER_KEY),
          expires: localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY_EXPIRES}`)
        },
        session: {
          token: sessionStorage.getItem(AUTH_CONFIG.TOKEN_KEY),
          refresh: sessionStorage.getItem(AUTH_CONFIG.REFRESH_TOKEN_KEY),
          user: sessionStorage.getItem(AUTH_CONFIG.USER_KEY),
          expires: sessionStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY_EXPIRES}`)
        }
      });
    } catch (e) {
      // some browsers may throw when accessing storage in certain contexts
      console.debug('AuthService.clearSession: storage debug failed', e);
    }

    // Clear both storages to be safe
    [localStorage, sessionStorage].forEach(storage => {
      storage.removeItem(AUTH_CONFIG.TOKEN_KEY);
      // Ensure refresh token is also removed (was previously omitted)
      storage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
      storage.removeItem(AUTH_CONFIG.USER_KEY);
      storage.removeItem(AUTH_CONFIG.REMEMBER_KEY);
      storage.removeItem(`${AUTH_CONFIG.TOKEN_KEY_EXPIRES}`);
    });

    console.debug('AuthService.clearSession: after clear', {
      localToken: localStorage.getItem(AUTH_CONFIG.TOKEN_KEY),
      sessionToken: sessionStorage.getItem(AUTH_CONFIG.TOKEN_KEY)
    });
  }

  private getSavedUser(): AuthUser | null {
    const userData = this.getStorageItem(AUTH_CONFIG.USER_KEY);
    return userData ? JSON.parse(userData) : null;
  }

  private hasValidToken(): boolean {
    const token = this.getToken();
    const expiration = this.getStorageItem(`${AUTH_CONFIG.TOKEN_KEY_EXPIRES}`);

    if (!token || !expiration) {
      console.debug('AuthService.hasValidToken: no token or expiration found');
      return false;
    }

    const expirationTime = parseInt(expiration, 10);
    const isValid = Date.now() < expirationTime;
    console.debug('AuthService.hasValidToken: token valid =', isValid, 'expires at', new Date(expirationTime));
    return isValid;
  }

  private checkTokenExpiration(): void {
    // Calculate remaining time until token expiry and setup auto-logout for that remaining time.
    // If there is no token or the token is already expired, clear session state without calling
    // logout() which triggers a backend request. This avoids racing with a fresh login flow that
    // may run during app initialization.
    const token = this.getToken();
    const expiration = this.getStorageItem(`${AUTH_CONFIG.TOKEN_KEY_EXPIRES}`);

    console.debug('AuthService.checkTokenExpiration:', { tokenPresent: !!token, expiration });

    if (!token || !expiration) {
      // No token found — ensure local subjects reflect signed-out state but don't call server logout.
      this.clearSession();
      this.currentUserSubject.next(null);
      this.isAuthenticatedSubject.next(false);
      return;
    }

    const expirationTime = parseInt(expiration, 10);
    const remainingMs = expirationTime - Date.now();

    console.debug('AuthService.checkTokenExpiration: remainingMs', remainingMs);

    if (remainingMs <= 0) {
      // Token already expired — clear session locally.
      this.clearSession();
      this.currentUserSubject.next(null);
      this.isAuthenticatedSubject.next(false);
      return;
    }

    // Setup auto-logout timer with remaining seconds (rounded up).
    const remainingSeconds = Math.ceil(remainingMs / 1000);
    this.setupAutoLogout(remainingSeconds);
  }

  private setupAutoLogout(expiresIn: number): void {
    // Clear existing timer if present
    if (this.logoutTimer) {
      clearTimeout(this.logoutTimer);
      this.logoutTimer = null;
    }

    // Ensure we use a sane default if expiresIn is invalid
    const safeExpires = Number.isFinite(Number(expiresIn)) && Number(expiresIn) > 0 ? Number(expiresIn) : 3600; // default 1 hour in seconds
    const ms = safeExpires * 1000; // Convert seconds to milliseconds

    console.debug('AuthService.setupAutoLogout: setting timer for', safeExpires, 'seconds (', ms, 'ms)');

    this.logoutTimer = setTimeout(() => {
      console.debug('AuthService.setupAutoLogout: auto-logout timer triggered');
      this.logout();
    }, ms);
  }

  private getStorageItem(key: string): string | null {
    // Check both storages, prioritize localStorage for rememberMe users
    return localStorage.getItem(key) || sessionStorage.getItem(key);
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client error: ${error.error.message}`;
    } else {
      // Server-side error
      switch (error.status) {
        case 400:
          errorMessage = 'Invalid credentials provided';
          break;
        case 401:
          errorMessage = 'Invalid email/phone or password';
          break;
        case 403:
          errorMessage = 'Account is disabled or suspended';
          break;
        case 429:
          errorMessage = 'Too many login attempts. Please try again later';
          break;
        case 500:
          errorMessage = 'Server error. Please try again later';
          break;
        default:
          errorMessage = `Server error: ${error.status}`;
      }
    }

    console.error('AuthService Error:', error);
    return throwError(() => new Error(errorMessage));
  }

  private clearPermissions(): void {
    this.permissionService.clearPermissions();
  }

  private loadUserPermissions(): Observable<any> {
    return this.permissionService.loadUserPermissions();
  }
}
