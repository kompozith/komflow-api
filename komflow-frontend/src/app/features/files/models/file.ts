// src/app/features/files/models/file.ts

export interface FileItem {
  id: string;
  name: string;
  originalName: string;
  mimeType: string;
  size: number;
  path: string;
  url: string;
  thumbnailUrl?: string;
  category: FileCategory;
  tags: string[];
  description?: string;
  uploadedBy: string;
  uploadedAt: string;
  lastModified: string;
  isPublic: boolean;
  downloadCount: number;
  checksum?: string;
}

export interface FilePage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: FileItem[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface FileFilters {
  page?: number;
  size?: number;
  sort?: string[];
  category?: FileCategory;
  search?: string;
  tags?: string[];
  uploadedBy?: string;
  dateFrom?: string;
  dateTo?: string;
  isPublic?: boolean;
}

export enum FileCategory {
  DOCUMENT = 'DOCUMENT',
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
  AUDIO = 'AUDIO',
  ARCHIVE = 'ARCHIVE',
  OTHER = 'OTHER'
}

export interface FileUploadRequest {
  file: File;
  category: FileCategory;
  tags?: string[];
  description?: string;
  isPublic?: boolean;
}

export interface FileUploadResponse {
  file: FileItem;
  uploadUrl?: string;
  success: boolean;
  message?: string;
}

export interface FileStats {
  totalFiles: number;
  totalSize: number;
  filesByCategory: { [key in FileCategory]: number };
  recentUploads: number;
  storageUsed: number;
  storageLimit: number;
}

export interface BulkFileOperation {
  fileIds: string[];
  operation: 'DELETE' | 'MOVE' | 'TAG' | 'UNTAG';
  targetCategory?: FileCategory;
  tags?: string[];
}

export interface FileShareLink {
  id: string;
  fileId: string;
  token: string;
  expiresAt: string;
  downloadLimit?: number;
  downloadsUsed: number;
  isActive: boolean;
  createdAt: string;
}
