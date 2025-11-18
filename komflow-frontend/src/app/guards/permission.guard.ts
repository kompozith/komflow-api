import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable, map, take } from 'rxjs';
import { PermissionService } from '../services/permission.service';

@Injectable({
  providedIn: 'root'
})
export class PermissionGuard implements CanActivate {

  constructor(
    private permissionService: PermissionService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {

    const requiredPermissions = route.data['permissions'] as string[];
    const requireAll = route.data['requireAll'] as boolean || false; // Default to OR logic

    if (!requiredPermissions || requiredPermissions.length === 0) {
      return true; // No permissions required
    }

    return this.permissionService.permissions$.pipe(
      take(1),
      map(permissions => {
        if (requireAll) {
          // User must have ALL specified permissions
          const hasAllPermissions = this.permissionService.hasAllPermissions(requiredPermissions);
          if (!hasAllPermissions) {
            this.router.navigate(['/authentication/login']);
            return false;
          }
          return true;
        } else {
          // User must have at least ONE of the specified permissions (OR logic)
          const hasAnyPermission = this.permissionService.hasAnyPermission(requiredPermissions);
          if (!hasAnyPermission) {
            this.router.navigate(['/authentication/login']);
            return false;
          }
          return true;
        }
      })
    );
  }
}
