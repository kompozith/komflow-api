import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate, CanActivateChild {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    return this.checkAuth(state);
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    return this.checkAuth(state);
  }

  private checkAuth(state: RouterStateSnapshot): boolean | UrlTree {
    const isAuth = this.authService.isAuthenticated();

    console.debug('AuthGuard.checkAuth: isAuthenticated =', isAuth, 'url =', state.url);

    if (isAuth) {
      return true;
    }

    // If not authenticated, redirect to the authentication login page with returnUrl.
    // Fall back to root ('/') if state.url is falsy.
    const returnUrl = state && state.url ? state.url : '/';
    console.debug('AuthGuard.checkAuth: redirecting to login with returnUrl =', returnUrl);
    // Return UrlTree to include the returnUrl as a query param.
    return this.router.createUrlTree(['/authentication/login'], { queryParams: { returnUrl } });
  }
}
