import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, map } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Permission {
  code: string;
  name: string;
  description: string;
  resource: string;
}

export interface UserPermissions {
  permissions: string[];
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private permissionsSubject = new BehaviorSubject<string[]>([]);
  public permissions$ = this.permissionsSubject.asObservable();

  private readonly API_BASE_URL = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {
    this.loadUserPermissions();
  }

  /**
   * Load user permissions from backend
   */
  loadUserPermissions(): Observable<UserPermissions> {
    return this.http.get<UserPermissions>(`${this.API_BASE_URL}/permissions`).pipe(
      map(response => {
        this.permissionsSubject.next(response.permissions);
        return response;
      })
    );
  }

  /**
   * Check if user has a specific permission
   */
  hasPermission(permissionCode: string): boolean {
    const permissions = this.permissionsSubject.value;
    return permissions.includes(permissionCode);
  }

  /**
   * Check if user has any of the specified permissions
   */
  hasAnyPermission(permissionCodes: string[]): boolean {
    return permissionCodes.some(code => this.hasPermission(code));
  }

  /**
   * Check if user has all of the specified permissions
   */
  hasAllPermissions(permissionCodes: string[]): boolean {
    return permissionCodes.every(code => this.hasPermission(code));
  }

  /**
   * Get all user permissions
   */
  getPermissions(): string[] {
    return this.permissionsSubject.value;
  }

  /**
   * Check contact permissions
   */
  canViewContacts(): boolean {
    return this.hasPermission('CONTACT_LIST');
  }

  canCreateContacts(): boolean {
    return this.hasPermission('CONTACT_CREATE');
  }

  canEditContacts(): boolean {
    return this.hasPermission('CONTACT_UPDATE');
  }

  canDeleteContacts(): boolean {
    return this.hasPermission('CONTACT_DELETE');
  }

  canViewContactDetails(): boolean {
    return this.hasPermission('CONTACT_SHOW');
  }

  /**
   * Check tag permissions
   */
  canViewTags(): boolean {
    return this.hasPermission('TAG_LIST');
  }

  canCreateTags(): boolean {
    return this.hasPermission('TAG_CREATE');
  }

  canEditTags(): boolean {
    return this.hasPermission('TAG_UPDATE');
  }

  canDeleteTags(): boolean {
    return this.hasPermission('TAG_DELETE');
  }

  canViewTagDetails(): boolean {
    return this.hasPermission('TAG_SHOW');
  }

  /**
   * Check message permissions
   */
  canViewMessages(): boolean {
    return this.hasPermission('MESSAGE_LIST');
  }

  canCreateMessages(): boolean {
    return this.hasPermission('MESSAGE_CREATE');
  }

  canEditMessages(): boolean {
    return this.hasPermission('MESSAGE_UPDATE');
  }

  canDeleteMessages(): boolean {
    return this.hasPermission('MESSAGE_DELETE');
  }

  canViewMessageDetails(): boolean {
    return this.hasPermission('MESSAGE_SHOW');
  }

  canSendMessagesToContact(): boolean {
    return this.hasPermission('MESSAGE_SEND_TO_CONTACT');
  }

  canSendMessagesToTag(): boolean {
    return this.hasPermission('MESSAGE_SEND_TO_TAG');
  }

  /**
   * Check campaign permissions
   */
  canViewCampaigns(): boolean {
    return this.hasPermission('CAMPAIGN_LIST');
  }

  canCreateCampaigns(): boolean {
    return this.hasPermission('CAMPAIGN_CREATE');
  }

  canEditCampaigns(): boolean {
    return this.hasPermission('CAMPAIGN_UPDATE');
  }

  canDeleteCampaigns(): boolean {
    return this.hasPermission('CAMPAIGN_DELETE');
  }

  canViewCampaignDetails(): boolean {
    return this.hasPermission('CAMPAIGN_SHOW');
  }

  canSendCampaigns(): boolean {
    return this.hasPermission('CAMPAIGN_SEND');
  }

  /**
   * Check personnel permissions
   */
  canViewPersonnel(): boolean {
    return this.hasPermission('PERSONNEL_VIEW');
  }

  canManagePersonnel(): boolean {
    return this.hasPermission('PERSONNEL_MANAGE');
  }

  /**
   * Check file permissions (generic for now)
   */
  canViewFiles(): boolean {
    return this.hasPermission('FILE_LIST') || this.hasPermission('FILE_READ');
  }

  canUploadFiles(): boolean {
    return this.hasPermission('FILE_UPLOAD');
  }

  canDeleteFiles(): boolean {
    return this.hasPermission('FILE_DELETE');
  }

  canShareFiles(): boolean {
    return this.hasPermission('FILE_SHARE');
  }

  /**
   * Check audit permissions
   */
  canViewAuditLogs(): boolean {
    return this.hasPermission('AUDIT_VIEW') || this.hasPermission('AUDIT_LIST');
  }

  canExportAuditLogs(): boolean {
    return this.hasPermission('AUDIT_EXPORT');
  }

  /**
   * Check admin permissions
   */
  isAdmin(): boolean {
    return this.hasPermission('ADMIN') || this.hasPermission('SUPER_ADMIN');
  }

  canManageUsers(): boolean {
    return this.hasPermission('USER_MANAGE') || this.isAdmin();
  }

  canManageRoles(): boolean {
    return this.hasPermission('ROLE_MANAGE') || this.isAdmin();
  }

  canManageBrands(): boolean {
    return this.hasPermission('BRAND_MANAGE') || this.isAdmin();
  }

  canManageStores(): boolean {
    return this.hasPermission('STORE_MANAGE') || this.isAdmin();
  }

  /**
   * Clear permissions (on logout)
   */
  clearPermissions(): void {
    this.permissionsSubject.next([]);
  }

  /**
   * Update permissions (after login or role change)
   */
  updatePermissions(permissions: string[]): void {
    this.permissionsSubject.next(permissions);
  }
}
