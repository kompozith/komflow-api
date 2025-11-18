// src/app/features/stores/models/store-category.ts

export interface StoreCategory {
  id: string;
  storeId: string;
  storeName: string;
  name: string;
  description?: string;
  productCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface StoreCategoryPage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: StoreCategory[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface StoreCategoryFilters {
  page?: number;
  size?: number;
  sort?: string[];
  storeId?: string;
  search?: string;
}

export interface CreateStoreCategoryRequest {
  name: string;
  description?: string;
}

export interface UpdateStoreCategoryRequest {
  name?: string;
  description?: string;
}