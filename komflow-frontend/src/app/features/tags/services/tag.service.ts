// src/app/features/tags/services/tag.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Tag, TagPage, TagFilters, CreateTagRequest, UpdateTagRequest } from '../models/tag';

@Injectable({
  providedIn: 'root',
})
export class TagService {
  private apiUrl = `${environment.apiUrl}/tags`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getTags(filters: TagFilters = {}): Observable<TagPage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.search) params = params.set('search', filters.search);

    const headers = this.getAuthHeaders();

    return this.http.get<TagPage>(this.apiUrl, { params, headers });
  }

  getTagById(id: string): Observable<Tag> {
    return this.http.get<Tag>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  createTag(tag: CreateTagRequest): Observable<Tag> {
    return this.http.post<Tag>(this.apiUrl, tag, { headers: this.getAuthHeaders() });
  }

  updateTag(id: string, tag: UpdateTagRequest): Observable<Tag> {
    return this.http.put<Tag>(`${this.apiUrl}/${id}`, tag, { headers: this.getAuthHeaders() });
  }

  deleteTag(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  toggleTagStatus(id: string, enabled: boolean): Observable<Tag> {
    return this.http.put<Tag>(`${this.apiUrl}/${id}/toggle-status`, { enabled }, { headers: this.getAuthHeaders() });
  }
}
