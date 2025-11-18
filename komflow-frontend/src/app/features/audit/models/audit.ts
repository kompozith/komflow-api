// src/app/features/audit/models/audit.ts

export interface AuditLog {
  id: string;
  timestamp: string;
  userId: string;
  username: string;
  action: AuditAction;
  resource: string;
  resourceId?: string;
  oldValue?: any;
  newValue?: any;
  ipAddress?: string;
  userAgent?: string;
  sessionId?: string;
  success: boolean;
  errorMessage?: string;
  metadata?: { [key: string]: any };
}

export enum AuditAction {
  CREATE = 'CREATE',
  READ = 'READ',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  LOGIN = 'LOGIN',
  LOGOUT = 'LOGOUT',
  PASSWORD_CHANGE = 'PASSWORD_CHANGE',
  PERMISSION_CHANGE = 'PERMISSION_CHANGE',
  FILE_UPLOAD = 'FILE_UPLOAD',
  FILE_DOWNLOAD = 'FILE_DOWNLOAD',
  CAMPAIGN_SEND = 'CAMPAIGN_SEND',
  CAMPAIGN_CANCEL = 'CAMPAIGN_CANCEL',
  BULK_OPERATION = 'BULK_OPERATION',
  EXPORT = 'EXPORT',
  IMPORT = 'IMPORT',
  SETTINGS_CHANGE = 'SETTINGS_CHANGE',
  OTHER = 'OTHER'
}

export interface AuditLogPage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: AuditLog[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface AuditFilters {
  page?: number;
  size?: number;
  sort?: string[];
  userId?: string;
  username?: string;
  action?: AuditAction;
  resource?: string;
  resourceId?: string;
  dateFrom?: string;
  dateTo?: string;
  success?: boolean;
  ipAddress?: string;
}

export interface AuditStats {
  totalLogs: number;
  logsByAction: { [key in AuditAction]: number };
  logsByResource: { [key: string]: number };
  recentActivity: number;
  failedOperations: number;
  topUsers: Array<{ userId: string; username: string; count: number }>;
}

export interface AuditExportRequest {
  filters: AuditFilters;
  format: 'CSV' | 'JSON' | 'PDF';
  includeMetadata: boolean;
}
