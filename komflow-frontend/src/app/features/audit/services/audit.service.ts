// src/app/features/audit/services/audit.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import {
  AuditLog,
  AuditLogPage,
  AuditFilters,
  AuditStats,
  AuditExportRequest,
  AuditAction
} from '../models/audit';
import {BadgeVariant} from "../../../shared/components/badge/badge.component";

@Injectable({
  providedIn: 'root',
})
export class AuditService {
  private apiUrl = `${environment.apiUrl}/audit`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getAuditLogs(filters: AuditFilters = {}): Observable<AuditLogPage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.userId) params = params.set('userId', filters.userId);
    if (filters.username) params = params.set('username', filters.username);
    if (filters.action) params = params.set('action', filters.action);
    if (filters.resource) params = params.set('resource', filters.resource);
    if (filters.resourceId) params = params.set('resourceId', filters.resourceId);
    if (filters.dateFrom) params = params.set('dateFrom', filters.dateFrom);
    if (filters.dateTo) params = params.set('dateTo', filters.dateTo);
    if (filters.success !== undefined) params = params.set('success', filters.success.toString());
    if (filters.ipAddress) params = params.set('ipAddress', filters.ipAddress);

    const headers = this.getAuthHeaders();

    return this.http.get<AuditLogPage>(this.apiUrl, { params, headers });
  }

  getAuditLogById(id: string): Observable<AuditLog> {
    return this.http.get<AuditLog>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  getAuditStats(): Observable<AuditStats> {
    return this.http.get<AuditStats>(`${this.apiUrl}/stats`, { headers: this.getAuthHeaders() });
  }

  exportAuditLogs(exportRequest: AuditExportRequest): Observable<Blob> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.apiUrl}/export`, exportRequest, {
      headers,
      responseType: 'blob'
    });
  }

  // Utility methods
  getActionColor(action: AuditAction): BadgeVariant {
    switch (action) {
      case AuditAction.CREATE: return 'success';
      case AuditAction.UPDATE: return 'info';
      case AuditAction.DELETE: return 'error';
      case AuditAction.LOGIN: return 'success';
      case AuditAction.LOGOUT: return 'secondary';
      case AuditAction.FILE_UPLOAD: return 'success';
      case AuditAction.FILE_DOWNLOAD: return 'info';
      case AuditAction.CAMPAIGN_SEND: return 'success';
      case AuditAction.CAMPAIGN_CANCEL: return 'warning';
      case AuditAction.BULK_OPERATION: return 'info';
      case AuditAction.EXPORT: return 'info';
      case AuditAction.IMPORT: return 'success';
      case AuditAction.SETTINGS_CHANGE: return 'warning';
      default: return 'secondary';
    }
  }

  getActionIcon(action: AuditAction): string {
    switch (action) {
      case AuditAction.CREATE: return 'add';
      case AuditAction.UPDATE: return 'edit';
      case AuditAction.DELETE: return 'delete';
      case AuditAction.READ: return 'visibility';
      case AuditAction.LOGIN: return 'login';
      case AuditAction.LOGOUT: return 'logout';
      case AuditAction.PASSWORD_CHANGE: return 'password';
      case AuditAction.PERMISSION_CHANGE: return 'security';
      case AuditAction.FILE_UPLOAD: return 'cloud_upload';
      case AuditAction.FILE_DOWNLOAD: return 'cloud_download';
      case AuditAction.CAMPAIGN_SEND: return 'send';
      case AuditAction.CAMPAIGN_CANCEL: return 'cancel';
      case AuditAction.BULK_OPERATION: return 'group_work';
      case AuditAction.EXPORT: return 'download';
      case AuditAction.IMPORT: return 'upload';
      case AuditAction.SETTINGS_CHANGE: return 'settings';
      default: return 'info';
    }
  }

  formatAuditValue(value: any): string {
    if (value === null || value === undefined) return '-';
    if (typeof value === 'object') {
      return JSON.stringify(value, null, 2);
    }
    return String(value);
  }

  getResourceDisplayName(resource: string): string {
    const resourceMap: { [key: string]: string } = {
      'CONTACT': 'Contact',
      'TAG': 'Tag',
      'MESSAGE': 'Message',
      'CAMPAIGN': 'Campaign',
      'USER': 'User',
      'ROLE': 'Role',
      'FILE': 'File',
      'AUDIT_LOG': 'Audit Log',
      'SETTINGS': 'Settings',
      'AUTH': 'Authentication'
    };
    return resourceMap[resource] || resource;
  }
}
