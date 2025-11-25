// src/app/features/tags/models/tag.ts

export interface Tag {
  id: number;
  name: string;
  colorCode?: string;
  color?: string;
  description?: string;
  enabled?: boolean;
  createdAt: string;
  updatedAt: string;
  contactCount?: number;
}

export interface TagPage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: Tag[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface TagFilters {
  page?: number;
  size?: number;
  sort?: string[];
  search?: string;
  startDate?: string;
  endDate?: string;
}

export interface CreateTagRequest {
  name: string;
  colorCode?: string;
  description?: string;
}

export interface UpdateTagRequest {
  name?: string;
  colorCode?: string;
  description?: string;
}
