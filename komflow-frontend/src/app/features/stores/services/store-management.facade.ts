import { Injectable } from '@angular/core';
import { Observable, combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';
import { StoreService } from './store.service';
import { StoreCategoryService } from './store-category.service';
import { Store, StorePage, StoreFilters, CreateStoreRequest, UpdateStoreRequest } from '../models/store';
import { StoreCategory, StoreCategoryPage, StoreCategoryFilters, CreateStoreCategoryRequest, UpdateStoreCategoryRequest } from '../models/store-category';
import { ProductCategory } from '../models/product-category';

@Injectable({
  providedIn: 'root'
})
export class StoreManagementFacade {

  constructor(
    private storeService: StoreService,
    private storeCategoryService: StoreCategoryService
  ) {}

  // Store operations
  getStores(filters: StoreFilters = {}): Observable<StorePage> {
    return this.storeService.getStores(filters).pipe(
      map((response: any) => {
        // Transform the backend response to match our StorePage interface
        return {
          totalElements: response.data.totalElements,
          totalPages: response.data.totalPages,
          size: response.data.size,
          content: response.data.content,
          number: response.data.number,
          sort: response.data.sort,
          numberOfElements: response.data.numberOfElements,
          pageable: response.data.pageable,
          first: response.data.first,
          last: response.data.last,
          empty: response.data.empty
        };
      })
    );
  }

  getStoreById(id: string): Observable<Store> {
    return this.storeService.getStoreById(id).pipe(
      map((response: any) => {
        console.log('Raw store response:', response);
        // Handle both wrapped and unwrapped responses
        return response.data || response;
      })
    );
  }

  createStore(store: CreateStoreRequest): Observable<Store> {
    return this.storeService.createStore(store).pipe(
      map((response: any) => response.data)
    );
  }

  updateStore(id: string, store: UpdateStoreRequest): Observable<Store> {
    return this.storeService.updateStore(id, store).pipe(
      map((response: any) => response.data)
    );
  }

  deleteStore(id: string): Observable<void> {
    return this.storeService.deleteStore(id);
  }

  // Store Category operations
  getStoreCategories(storeId: string, filters: StoreCategoryFilters = {}): Observable<StoreCategoryPage> {
    return this.storeCategoryService.getStoreCategories(storeId, filters).pipe(
      map((response: any) => {
        console.log('Raw categories response:', response);
        // Handle both wrapped and unwrapped responses
        const data = response.data || response;
        // Transform the backend response to match our StoreCategoryPage interface
        return {
          totalElements: data.totalElements,
          totalPages: data.totalPages,
          size: data.size,
          content: data.content,
          number: data.number,
          sort: data.sort,
          numberOfElements: data.numberOfElements,
          pageable: data.pageable,
          first: data.first,
          last: data.last,
          empty: data.empty
        };
      })
    );
  }

  createStoreCategory(storeId: string, category: CreateStoreCategoryRequest): Observable<StoreCategory> {
    return this.storeCategoryService.createStoreCategory(storeId, category).pipe(
      map((response: any) => response.data)
    );
  }

  updateStoreCategory(storeId: string, categoryId: string, category: UpdateStoreCategoryRequest): Observable<StoreCategory> {
    return this.storeCategoryService.updateStoreCategory(storeId, categoryId, category).pipe(
      map((response: any) => response.data)
    );
  }

  deleteStoreCategory(storeId: string, categoryId: string): Observable<void> {
    return this.storeCategoryService.deleteStoreCategory(storeId, categoryId);
  }

  // Combined operations for dashboard/stats
  getStoreStats(): Observable<{
    totalStores: number;
    totalSellers: number;
    totalProducts: number;
  }> {
    return this.getStores({ size: 1000 }).pipe(
      map(page => {
        const stores = page.content;
        return {
          totalStores: page.totalElements,
          totalSellers: stores.reduce((sum, store) => sum + store.sellerCount, 0),
          totalProducts: stores.reduce((sum, store) => sum + store.productCount, 0)
        };
      })
    );
  }

  // Search and filter helpers
  searchStores(query: string, filters: StoreFilters = {}): Observable<StorePage> {
    return this.getStores({ ...filters, search: query });
  }

  getStoresByBrand(brandId: string): Observable<StorePage> {
    return this.getStores({ brandId, size: 100 });
  }

  getStoresByCity(city: string): Observable<StorePage> {
    return this.getStores({ city, size: 100 });
  }

  // Product Category operations
  getStoreProductCategories(storeId: string): Observable<ProductCategory[]> {
    return this.storeService.getStoreProductCategories(storeId);
  }

  createStoreProductCategory(storeId: string, category: { name: string; description: string }): Observable<ProductCategory> {
    return this.storeService.createStoreProductCategory(storeId, category).pipe(
      map((response: any) => {
        const data = response.data;
        return {
          ...data,
          createdAt: new Date(data.createdAt),
          updatedAt: new Date(data.updatedAt)
        };
      })
    );
  }

  updateStoreProductCategory(storeId: string, categoryId: string, category: { name: string; description: string; storeId: string }): Observable<ProductCategory> {
    return this.storeService.updateStoreProductCategory(storeId, categoryId, category).pipe(
      map((response: any) => {
        const data = response.data;
        return {
          ...data,
          createdAt: new Date(data.createdAt),
          updatedAt: new Date(data.updatedAt)
        };
      })
    );
  }

  deleteStoreProductCategory(storeId: string, categoryId: string): Observable<void> {
    return this.storeService.deleteStoreProductCategory(storeId, categoryId);
  }
}