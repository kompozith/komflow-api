// src/app/features/campaigns/services/campaign.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import {
  Campaign,
  CampaignPage,
  CampaignFilters,
  CreateCampaignRequest,
  UpdateCampaignRequest,
  CampaignSendRequest,
  CampaignStats,
  CampaignRecipient
} from '../models/campaign';

@Injectable({
  providedIn: 'root',
})
export class CampaignService {
  private apiUrl = `${environment.apiUrl}/campaigns`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getCampaigns(filters: CampaignFilters = {}): Observable<CampaignPage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.status) params = params.set('status', filters.status);
    if (filters.messageId) params = params.set('messageId', filters.messageId);
    if (filters.tagId) params = params.set('tagId', filters.tagId);
    if (filters.search) params = params.set('search', filters.search);
    if (filters.dateFrom) params = params.set('dateFrom', filters.dateFrom);
    if (filters.dateTo) params = params.set('dateTo', filters.dateTo);

    const headers = this.getAuthHeaders();

    return this.http.get<CampaignPage>(this.apiUrl, { params, headers });
  }

  getCampaignById(id: string): Observable<Campaign> {
    return this.http.get<Campaign>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  createCampaign(campaign: CreateCampaignRequest): Observable<Campaign> {
    return this.http.post<Campaign>(this.apiUrl, campaign, { headers: this.getAuthHeaders() });
  }

  updateCampaign(id: string, campaign: UpdateCampaignRequest): Observable<Campaign> {
    return this.http.put<Campaign>(`${this.apiUrl}/${id}`, campaign, { headers: this.getAuthHeaders() });
  }

  deleteCampaign(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  sendCampaign(sendRequest: CampaignSendRequest): Observable<Campaign> {
    return this.http.post<Campaign>(`${this.apiUrl}/send`, sendRequest, { headers: this.getAuthHeaders() });
  }

  cancelCampaign(id: string): Observable<Campaign> {
    return this.http.post<Campaign>(`${this.apiUrl}/${id}/cancel`, {}, { headers: this.getAuthHeaders() });
  }

  getCampaignRecipients(campaignId: string): Observable<CampaignRecipient[]> {
    return this.http.get<CampaignRecipient[]>(`${this.apiUrl}/${campaignId}/recipients`, { headers: this.getAuthHeaders() });
  }

  getCampaignStats(): Observable<CampaignStats> {
    return this.http.get<CampaignStats>(`${this.apiUrl}/stats`, { headers: this.getAuthHeaders() });
  }

  // Preview campaign recipients before sending
  previewCampaignRecipients(campaignId: string): Observable<{ count: number; sampleRecipients: any[] }> {
    return this.http.get<{ count: number; sampleRecipients: any[] }>(`${this.apiUrl}/${campaignId}/preview`, { headers: this.getAuthHeaders() });
  }
}
