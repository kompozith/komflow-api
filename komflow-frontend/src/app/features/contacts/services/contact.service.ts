// src/app/features/contacts/services/contact.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Contact, ContactPage, ContactFilters, CreateContactRequest, UpdateContactRequest } from '../models/contact';

@Injectable({
  providedIn: 'root',
})
export class ContactService {
  private apiUrl = `${environment.apiUrl}/contacts`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getContacts(filters: ContactFilters = {}): Observable<ContactPage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.search) params = params.set('search', filters.search);
    if (filters.tagId) params = params.set('tagId', filters.tagId);

    const headers = this.getAuthHeaders();

    return this.http.get<ContactPage>(this.apiUrl, { params, headers });
  }

  getContactById(id: string): Observable<Contact> {
    return this.http.get<Contact>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  createContact(contact: CreateContactRequest): Observable<Contact> {
    return this.http.post<Contact>(this.apiUrl, contact, { headers: this.getAuthHeaders() });
  }

  updateContact(id: string, contact: UpdateContactRequest): Observable<Contact> {
    return this.http.put<Contact>(`${this.apiUrl}/${id}`, contact, { headers: this.getAuthHeaders() });
  }

  deleteContact(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }
}
