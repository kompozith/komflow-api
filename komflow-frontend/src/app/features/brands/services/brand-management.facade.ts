import { Injectable } from '@angular/core';
import { Observable, combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';
import { BrandService } from './brand.service';
import { Brand, BrandFilters, BrandPage, CreateBrandRequest, UpdateBrandRequest, BrandType } from '../models/brand';

@Injectable({
  providedIn: 'root'
})
export class BrandManagementFacade {

  constructor(
    private brandService: BrandService
  ) {}

  // Brand operations
  getBrands(filters: BrandFilters = {}): Observable<BrandPage> {
    return this.brandService.getBrands(filters).pipe(
      map((response: any) => {
        // Transform the backend response to match our BrandPage interface
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

  getBrandById(id: string): Observable<Brand> {
    return this.brandService.getBrandById(id);
  }

  createBrand(brand: CreateBrandRequest): Observable<Brand> {
    return this.brandService.createBrand(brand);
  }

  updateBrand(id: string, brand: UpdateBrandRequest): Observable<Brand> {
    return this.brandService.updateBrand(id, brand);
  }

  deleteBrand(id: string): Observable<void> {
    return this.brandService.deleteBrand(id);
  }

  // Combined operations for dashboard/stats
  getBrandStats(): Observable<{
    totalBrands: number;
    supermarketBrands: number;
    restaurantBrands: number;
    pharmacyBrands: number;
    brandsWithStores: number;
  }> {
    return this.getBrands({ size: 1000 }).pipe(
      map(page => {
        const brands = page.content;
        return {
          totalBrands: page.totalElements,
          supermarketBrands: brands.filter((b: Brand) => b.type === 'SUPERMARKET').length,
          restaurantBrands: brands.filter((b: Brand) => b.type === 'RESTAURANT').length,
          pharmacyBrands: brands.filter((b: Brand) => b.type === 'PHARMACY').length,
          brandsWithStores: brands.filter((b: Brand) => b.storeCount > 0).length
        };
      })
    );
  }

  // Bulk operations
  bulkDeleteBrands(brandIds: string[]): Observable<void[]> {
    const operations = brandIds.map(id => this.deleteBrand(id));
    return combineLatest(operations);
  }

  // Search and filter helpers
  searchBrands(query: string, filters: BrandFilters = {}): Observable<BrandPage> {
    return this.getBrands({ ...filters, search: query });
  }

  getBrandsByType(type: BrandType): Observable<BrandPage> {
    return this.getBrands({ type, size: 100 });
  }

  getBrandsWithStores(): Observable<BrandPage> {
    return this.getBrands({ hasStores: true, size: 100 });
  }
}