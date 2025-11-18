// src/app/features/stores/services/store-category.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { StoreCategory, StoreCategoryPage, StoreCategoryFilters, CreateStoreCategoryRequest, UpdateStoreCategoryRequest } from '../models/store-category';

@Injectable({
  providedIn: 'root',
})
export class StoreCategoryService {
  private apiUrl = `${environment.apiUrl}/stores`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getStoreCategories(storeId: string, filters: StoreCategoryFilters = {}): Observable<StoreCategoryPage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.search) params = params.set('search', filters.search);

    const headers = this.getAuthHeaders();

    return this.http.get<any>(`${this.apiUrl}/${storeId}/categories`, { params, headers });
  }

  createStoreCategory(storeId: string, category: CreateStoreCategoryRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${storeId}/categories`, category, { headers: this.getAuthHeaders() });
  }

  updateStoreCategory(storeId: string, categoryId: string, category: UpdateStoreCategoryRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${storeId}/categories/${categoryId}`, category, { headers: this.getAuthHeaders() });
  }

  deleteStoreCategory(storeId: string, categoryId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${storeId}/categories/${categoryId}`, { headers: this.getAuthHeaders() });
  }
}