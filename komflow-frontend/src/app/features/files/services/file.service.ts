// src/app/features/files/services/file.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders, HttpRequest, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import {
  FileItem,
  FilePage,
  FileFilters,
  FileUploadRequest,
  FileUploadResponse,
  FileStats,
  BulkFileOperation,
  FileShareLink,
  FileCategory
} from '../models/file';

@Injectable({
  providedIn: 'root',
})
export class FileService {
  private apiUrl = `${environment.apiUrl}/files`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
  }

  getFiles(filters: FileFilters = {}): Observable<FilePage> {
    let params = new HttpParams();

    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());
    if (filters.sort && filters.sort.length > 0) {
      filters.sort.forEach(sort => params = params.append('sort', sort));
    }
    if (filters.category) params = params.set('category', filters.category);
    if (filters.search) params = params.set('search', filters.search);
    if (filters.tags && filters.tags.length > 0) {
      filters.tags.forEach(tag => params = params.append('tags', tag));
    }
    if (filters.uploadedBy) params = params.set('uploadedBy', filters.uploadedBy);
    if (filters.dateFrom) params = params.set('dateFrom', filters.dateFrom);
    if (filters.dateTo) params = params.set('dateTo', filters.dateTo);
    if (filters.isPublic !== undefined) params = params.set('isPublic', filters.isPublic.toString());

    const headers = this.getAuthHeaders();

    return this.http.get<FilePage>(this.apiUrl, { params, headers });
  }

  getFileById(id: string): Observable<FileItem> {
    return this.http.get<FileItem>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  uploadFile(fileData: FileUploadRequest): Observable<HttpEvent<FileUploadResponse>> {
    const formData = new FormData();
    formData.append('file', fileData.file);
    formData.append('category', fileData.category);
    if (fileData.tags && fileData.tags.length > 0) {
      formData.append('tags', JSON.stringify(fileData.tags));
    }
    if (fileData.description) {
      formData.append('description', fileData.description);
    }
    if (fileData.isPublic !== undefined) {
      formData.append('isPublic', fileData.isPublic.toString());
    }

    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      ...(token && { 'Authorization': `Bearer ${token}` })
    });

    const req = new HttpRequest('POST', this.apiUrl, formData, {
      headers,
      reportProgress: true
    });

    return this.http.request<FileUploadResponse>(req);
  }

  deleteFile(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  downloadFile(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/download`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  updateFile(id: string, updates: Partial<FileItem>): Observable<FileItem> {
    return this.http.put<FileItem>(`${this.apiUrl}/${id}`, updates, { headers: this.getAuthHeaders() });
  }

  // Bulk operations
  bulkDelete(fileIds: string[]): Observable<{ deleted: number; failed: number }> {
    const operation: BulkFileOperation = {
      fileIds,
      operation: 'DELETE'
    };
    return this.http.post<{ deleted: number; failed: number }>(`${this.apiUrl}/bulk`, operation, { headers: this.getAuthHeaders() });
  }

  bulkTag(fileIds: string[], tags: string[]): Observable<{ tagged: number; failed: number }> {
    const operation: BulkFileOperation = {
      fileIds,
      operation: 'TAG',
      tags
    };
    return this.http.post<{ tagged: number; failed: number }>(`${this.apiUrl}/bulk`, operation, { headers: this.getAuthHeaders() });
  }

  bulkUntag(fileIds: string[], tags: string[]): Observable<{ untagged: number; failed: number }> {
    const operation: BulkFileOperation = {
      fileIds,
      operation: 'UNTAG',
      tags
    };
    return this.http.post<{ untagged: number; failed: number }>(`${this.apiUrl}/bulk`, operation, { headers: this.getAuthHeaders() });
  }

  bulkMoveCategory(fileIds: string[], category: FileCategory): Observable<{ moved: number; failed: number }> {
    const operation: BulkFileOperation = {
      fileIds,
      operation: 'MOVE',
      targetCategory: category
    };
    return this.http.post<{ moved: number; failed: number }>(`${this.apiUrl}/bulk`, operation, { headers: this.getAuthHeaders() });
  }

  // File sharing
  createShareLink(fileId: string, expiresIn?: number, downloadLimit?: number): Observable<FileShareLink> {
    const body = {
      fileId,
      expiresIn,
      downloadLimit
    };
    return this.http.post<FileShareLink>(`${this.apiUrl}/share`, body, { headers: this.getAuthHeaders() });
  }

  getShareLinks(fileId: string): Observable<FileShareLink[]> {
    return this.http.get<FileShareLink[]>(`${this.apiUrl}/${fileId}/share`, { headers: this.getAuthHeaders() });
  }

  revokeShareLink(shareId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/share/${shareId}`, { headers: this.getAuthHeaders() });
  }

  // Statistics
  getFileStats(): Observable<FileStats> {
    return this.http.get<FileStats>(`${this.apiUrl}/stats`, { headers: this.getAuthHeaders() });
  }

  // Utility methods
  getFileIcon(mimeType: string): string {
    if (mimeType.startsWith('image/')) return 'image';
    if (mimeType.startsWith('video/')) return 'videocam';
    if (mimeType.startsWith('audio/')) return 'audiotrack';
    if (mimeType.includes('pdf')) return 'picture_as_pdf';
    if (mimeType.includes('zip') || mimeType.includes('rar')) return 'archive';
    if (mimeType.includes('text')) return 'description';
    return 'insert_drive_file';
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  isImageFile(mimeType: string): boolean {
    return mimeType.startsWith('image/');
  }

  isVideoFile(mimeType: string): boolean {
    return mimeType.startsWith('video/');
  }

  isAudioFile(mimeType: string): boolean {
    return mimeType.startsWith('audio/');
  }
}
