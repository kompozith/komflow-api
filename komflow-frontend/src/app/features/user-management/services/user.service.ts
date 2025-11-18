// src/app/features/user-management/services/user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AUTH_CONFIG } from 'src/app/features/authentication/auth.constants';
import { User, UserPage, UserFilters } from '../models/user';
import { USER_MANAGEMENT_API, UserRole, UserStatus } from '../user-management.constants';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/${USER_MANAGEMENT_API.BASE}`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getUsers(filters: UserFilters = {}): Observable<UserPage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sortBy) params = params.set('sortBy', filters.sortBy);
    if (filters.sortDirection) params = params.set('sortDirection', filters.sortDirection);
    if (filters.role) params = params.set('role', filters.role);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.search) params = params.set('search', filters.search);
    if (filters.registrationDateFrom) params = params.set('registrationDateFrom', filters.registrationDateFrom);
    if (filters.registrationDateTo) params = params.set('registrationDateTo', filters.registrationDateTo);
    if (filters.lastUpdatedFrom) params = params.set('lastUpdatedFrom', filters.lastUpdatedFrom);
    if (filters.lastUpdatedTo) params = params.set('lastUpdatedTo', filters.lastUpdatedTo);

    const headers = this.getAuthHeaders();

    return this.http.get<UserPage>(this.apiUrl, { params, headers });
  }

  getUserById(id: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  createUser(user: Partial<User>): Observable<User> {
    return this.http.post<User>(this.apiUrl, user, { headers: this.getAuthHeaders() });
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user, { headers: this.getAuthHeaders() });
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  updateUserStatus(id: string, status: UserStatus): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}/status`, { status: status }, { headers: this.getAuthHeaders() });
  }

  updateUserRole(id: string, newRole: UserRole, reason?: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}/role`, { newRole, reason }, { headers: this.getAuthHeaders() });
  }

  promoteToAdmin(id: string, targetRole: UserRole, reason?: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}/promote-to-admin`, { targetRole, reason }, { headers: this.getAuthHeaders() });
  }
}