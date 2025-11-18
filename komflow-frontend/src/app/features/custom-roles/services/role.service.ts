import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Role, RoleDetail, RolePage, RoleFilters, CreateRoleRequest, UpdateRoleRequest, Policy } from '../models/role';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private apiUrl = `${environment.apiUrl}/roles`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getRoles(filters: RoleFilters = {}): Observable<RolePage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sortBy) params = params.set('sortBy', filters.sortBy);
    if (filters.sortDirection) params = params.set('sortDirection', filters.sortDirection);
    if (filters.search) params = params.set('search', filters.search);
    if (filters.isActive !== undefined) params = params.set('isActive', filters.isActive.toString());
    if (filters.isSystem !== undefined) params = params.set('isSystem', filters.isSystem.toString());

    const headers = this.getAuthHeaders();

    return this.http.get<RolePage>(this.apiUrl, { params, headers });
  }

  getRoleById(id: string): Observable<RoleDetail> {
    return this.http.get<RoleDetail>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  createRole(role: CreateRoleRequest): Observable<Role> {
    return this.http.post<Role>(this.apiUrl, role, { headers: this.getAuthHeaders() });
  }

  updateRole(id: string, role: UpdateRoleRequest): Observable<Role> {
    return this.http.put<Role>(`${this.apiUrl}/${id}`, role, { headers: this.getAuthHeaders() });
  }

  deleteRole(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  getPolicies(resource?: string): Observable<Policy[]> {
    let params = new HttpParams();
    if (resource) {
      params = params.set('resource', resource);
    }
    return this.http.get<Policy[]>(`${environment.apiUrl}/policies`, {
      params,
      headers: this.getAuthHeaders()
    });
  }

  assignPoliciesToRole(roleId: string, policyIds: string[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${roleId}/policies`, {
      roleId,
      policyIds
    }, { headers: this.getAuthHeaders() });
  }
}