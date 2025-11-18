// src/app/features/stores/models/store.ts

export interface Address {
  id: string;
  streetAddress: string;
  city: string;
  region: string;
  postalCode: string;
  country: string;
  latitude: number;
  longitude: number;
  landmark?: string;
  instructions?: string;
  fullAddress: string;
  hasCoordinates: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BrandSummary {
  id: string;
  name: string;
  type: string;
  logoUrl?: string;
}

export interface Store {
  id: string;
  name: string;
  phone: string;
  address: Address;
  description?: string;
  logoUrl?: string;
  status: string;
  brand: BrandSummary;
  sellerCount: number;
  productCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface StorePage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: Store[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface StoreFilters {
  page?: number;
  size?: number;
  sort?: string[];
  brandId?: string;
  search?: string;
  city?: string;
  region?: string;
}

export interface CreateStoreRequest {
  name: string;
  phone: string;
  address: {
    streetAddress: string;
    city: string;
    region: string;
    postalCode: string;
    country: string;
    latitude?: number;
    longitude?: number;
    landmark?: string;
    instructions?: string;
  };
  description?: string;
  brandId: string;
}

export interface UpdateStoreRequest {
  name?: string;
  phone?: string;
  address?: {
    streetAddress?: string;
    city?: string;
    region?: string;
    postalCode?: string;
    country?: string;
    latitude?: number;
    longitude?: number;
    landmark?: string;
    instructions?: string;
  };
  description?: string;
  brandId?: string;
}