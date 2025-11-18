// src/app/features/brands/models/brand.ts

export type BrandType = 'SUPERMARKET' | 'RESTAURANT' | 'PHARMACY';

export interface Brand {
  id: string;
  name: string;
  type: BrandType;
  description?: string;
  logo?: string;
  storeCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface BrandPage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: Brand[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface BrandFilters {
  page?: number;
  size?: number;
  sort?: string[];
  type?: BrandType;
  search?: string;
  hasStores?: boolean;
}

export interface CreateBrandRequest {
  name: string;
  type: BrandType;
  description?: string;
  logo?: File;
}

export interface UpdateBrandRequest {
  name?: string;
  type?: BrandType;
  description?: string;
  logo?: File;
}