// src/app/features/campaigns/models/campaign.ts

export interface CampaignRecipient {
  id: string;
  contactId: string;
  contactName: string;
  contactEmail?: string;
  contactPhone?: string;
  status: 'PENDING' | 'SENT' | 'DELIVERED' | 'FAILED';
  sentAt?: string;
  deliveredAt?: string;
  errorMessage?: string;
}

export interface Campaign {
  id: string;
  name: string;
  description?: string;
  messageId: string;
  message?: {
    id: string;
    name: string;
    channel: string;
    type: string;
  };
  tagIds: string[];
  tags?: {
    id: string;
    name: string;
    color?: string;
  }[];
  status: CampaignStatus;
  scheduledAt?: string;
  sentAt?: string;
  completedAt?: string;
  totalRecipients: number;
  sentCount: number;
  deliveredCount: number;
  failedCount: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface CampaignPage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: Campaign[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface CampaignFilters {
  page?: number;
  size?: number;
  sort?: string[];
  status?: CampaignStatus;
  messageId?: string;
  tagId?: string;
  search?: string;
  dateFrom?: string;
  dateTo?: string;
}

export enum CampaignStatus {
  DRAFT = 'DRAFT',
  SCHEDULED = 'SCHEDULED',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  FAILED = 'FAILED'
}

export interface CreateCampaignRequest {
  name: string;
  description?: string;
  messageId: string;
  tagIds: string[];
  scheduledAt?: string;
}

export interface UpdateCampaignRequest {
  name?: string;
  description?: string;
  messageId?: string;
  tagIds?: string[];
  scheduledAt?: string;
  status?: CampaignStatus;
}

export interface CampaignSendRequest {
  campaignId: string;
}

export interface CampaignStats {
  totalCampaigns: number;
  activeCampaigns: number;
  completedCampaigns: number;
  totalMessagesSent: number;
  totalMessagesDelivered: number;
  deliveryRate: number;
}
