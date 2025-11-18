// src/app/features/stores/services/store.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Store, StorePage, StoreFilters, CreateStoreRequest, UpdateStoreRequest } from '../models/store';
import { ProductCategory } from '../models/product-category';

@Injectable({
  providedIn: 'root',
})
export class StoreService {
  private apiUrl = `${environment.apiUrl}/stores`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getStores(filters: StoreFilters = {}): Observable<StorePage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.brandId) params = params.set('brandId', filters.brandId);
    if (filters.search) params = params.set('search', filters.search);
    if (filters.city) params = params.set('city', filters.city);
    if (filters.region) params = params.set('region', filters.region);

    const headers = this.getAuthHeaders();

    return this.http.get<any>(this.apiUrl, { params, headers });
  }

  getStoreById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  createStore(store: CreateStoreRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, store, { headers: this.getAuthHeaders() });
  }

  updateStore(id: string, store: UpdateStoreRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, store, { headers: this.getAuthHeaders() });
  }

  deleteStore(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  getStoreProductCategories(storeId: string): Observable<ProductCategory[]> {
    return this.http.get<any>(`${this.apiUrl}/${storeId}/categories`, { headers: this.getAuthHeaders() }).pipe(
      map(response => {
        const categories = response.data || [];
        return categories.map((cat: any) => ({
          ...cat,
          createdAt: new Date(cat.createdAt),
          updatedAt: new Date(cat.updatedAt)
        }));
      })
    );
  }

  createStoreProductCategory(storeId: string, category: { name: string; description: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${storeId}/categories`, category, { headers: this.getAuthHeaders() });
  }

  updateStoreProductCategory(storeId: string, categoryId: string, category: { name: string; description: string; storeId: string }): Observable<any> {
    return this.http.patch<any>(`${environment.apiUrl}/categories/${categoryId}`, category, { headers: this.getAuthHeaders() });
  }

  deleteStoreProductCategory(storeId: string, categoryId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${storeId}/categories/${categoryId}`, { headers: this.getAuthHeaders() });
  }
}