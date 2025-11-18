import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Guest Guard (Inverse Auth Guard)
 * 
 * Prevents authenticated users from accessing authentication pages.
 * If user is already authenticated, redirects to home page.
 * If user is not authenticated, allows access to the route.
 */
@Injectable({
  providedIn: 'root'
})
export class GuestGuard implements CanActivate, CanActivateChild {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    return this.checkGuest(state);
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    return this.checkGuest(state);
  }

  private checkGuest(state: RouterStateSnapshot): boolean | UrlTree {
    const isAuth = this.authService.isAuthenticated();

    if (!isAuth) {
      // User is not authenticated, allow access to authentication pages
      return true;
    }

    // User is already authenticated, redirect to home page
    console.log('GuestGuard: User already authenticated, redirecting to home');
    return this.router.createUrlTree(['/']);
  }
}