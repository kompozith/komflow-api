// src/app/features/tags/models/tag.ts

export interface Tag {
  id: number;
  name: string;
  colorCode?: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
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
}

export interface CreateTagRequest {
  name: string;
  color?: string;
  description?: string;
}

export interface UpdateTagRequest {
  name?: string;
  color?: string;
  description?: string;
}
