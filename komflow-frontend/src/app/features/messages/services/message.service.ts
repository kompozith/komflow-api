// src/app/features/messages/services/message.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import {
  Message,
  MessagePage,
  MessageFilters,
  CreateMessageRequest,
  UpdateMessageRequest,
  SendMessageRequest,
  MessageSendResult
} from '../models/message';

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  private apiUrl = `${environment.apiUrl}/messages`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getMessages(filters: MessageFilters = {}): Observable<MessagePage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.channel) params = params.set('channel', filters.channel);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.search) params = params.set('search', filters.search);

    const headers = this.getAuthHeaders();

    return this.http.get<MessagePage>(this.apiUrl, { params, headers });
  }

  getMessageById(id: string): Observable<Message> {
    return this.http.get<Message>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  createMessage(message: CreateMessageRequest): Observable<Message> {
    return this.http.post<Message>(this.apiUrl, message, { headers: this.getAuthHeaders() });
  }

  updateMessage(id: string, message: UpdateMessageRequest): Observable<Message> {
    return this.http.put<Message>(`${this.apiUrl}/${id}`, message, { headers: this.getAuthHeaders() });
  }

  deleteMessage(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  sendMessage(sendRequest: SendMessageRequest): Observable<MessageSendResult[]> {
    return this.http.post<MessageSendResult[]>(`${this.apiUrl}/send`, sendRequest, { headers: this.getAuthHeaders() });
  }

  // Test message sending with specific contact
  testMessage(messageId: string, contactId: string, variableValues?: { [key: string]: string }): Observable<MessageSendResult> {
    const body = { contactId, variableValues };
    return this.http.post<MessageSendResult>(`${this.apiUrl}/${messageId}/test`, body, { headers: this.getAuthHeaders() });
  }
}
