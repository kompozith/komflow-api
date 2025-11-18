// src/app/features/brands/services/brand.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Brand, BrandPage, BrandFilters, CreateBrandRequest, UpdateBrandRequest } from '../models/brand';

@Injectable({
  providedIn: 'root',
})
export class BrandService {
  private apiUrl = `${environment.apiUrl}/brands`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getBrands(filters: BrandFilters = {}): Observable<BrandPage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.type) params = params.set('type', filters.type);
    if (filters.search) params = params.set('search', filters.search);
    if (filters.hasStores !== undefined) params = params.set('hasStores', filters.hasStores.toString());

    const headers = this.getAuthHeaders();

    return this.http.get<any>(this.apiUrl, { params, headers });
  }

  getBrandById(id: string): Observable<Brand> {
    return this.http.get<Brand>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  createBrand(brand: CreateBrandRequest): Observable<Brand> {
    const formData = new FormData();
    formData.append('name', brand.name);
    formData.append('type', brand.type);
    if (brand.description) {
      formData.append('description', brand.description);
    }
    if (brand.logo) {
      formData.append('logo', brand.logo);
    }

    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      ...(token && { 'Authorization': `Bearer ${token}` })
    });

    return this.http.post<Brand>(this.apiUrl, formData, { headers });
  }

  updateBrand(id: string, brand: UpdateBrandRequest): Observable<Brand> {
    const formData = new FormData();
    if (brand.name) formData.append('name', brand.name);
    if (brand.type) formData.append('type', brand.type);
    if (brand.description) formData.append('description', brand.description);
    if (brand.logo) formData.append('logo', brand.logo);

    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      ...(token && { 'Authorization': `Bearer ${token}` })
    });

    return this.http.put<Brand>(`${this.apiUrl}/${id}`, formData, { headers });
  }

  deleteBrand(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }
}