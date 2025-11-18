import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { DatePipe } from '@angular/common';
import { StoreManagementFacade } from '../../services/store-management.facade';
import { Store } from '../../models/store';
import { StoreCategory } from '../../models/store-category';
import { ProductCategory } from '../../models/product-category';
import { ProductCategoriesComponent } from './product-categories/product-categories.component';

@Component({
  selector: 'app-store-details',
  templateUrl: './store-details.component.html',
  styleUrls: ['./store-details.component.scss'],
  imports: [MaterialModule, TablerIconsModule, CommonModule, DatePipe, ProductCategoriesComponent],
})
export class StoreDetailsComponent implements OnInit, AfterViewInit {
  store: Store | null = null;
  isLoading = true;
  storeCategories: StoreCategory[] = [];
  productCategories: ProductCategory[] = [];
  productCategoriesDataSource = new MatTableDataSource<ProductCategory>();
  displayedCategoryColumns: string[] = ['name', 'type', 'description', 'productCount', 'priority', 'status', 'createdAt'];
  displayedProductCategoryColumns: string[] = ['name', 'description', 'productCount', 'createdAt', 'updatedAt', 'actions'];

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private storeManagementFacade: StoreManagementFacade,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const storeId = this.route.snapshot.params['id'];
    if (storeId) {
      this.loadStoreDetails(storeId);
    }
  }

  ngAfterViewInit(): void {
    this.productCategoriesDataSource.sort = this.sort;
    this.productCategoriesDataSource.paginator = this.paginator;
  }

  loadStoreDetails(storeId: string): void {
    this.isLoading = true;
    this.storeManagementFacade.getStoreById(storeId).subscribe({
      next: (store) => {
        console.log('Store data received:', store);
        this.store = store;
        this.isLoading = false; // Set loading to false immediately after store data is loaded
        this.loadStoreCategories(storeId); // Load categories separately
        this.loadProductCategories(storeId); // Load product categories
      },
      error: (error) => {
        console.error('Error loading store details:', error);
        this.snackBar.open('Error loading store details', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.router.navigate(['../']);
      }
    });
  }

  loadStoreCategories(storeId: string): void {
    this.storeManagementFacade.getStoreCategories(storeId, { size: 5 }).subscribe({
      next: (response) => {
        console.log('Store categories received:', response);
        this.storeCategories = response.content;
        // No need to set isLoading here since it's already false
      },
      error: (error) => {
        console.error('Error loading store categories:', error);
        this.storeCategories = [];
        // Categories loading failure shouldn't prevent store display
      }
    });
  }

  loadProductCategories(storeId: string): void {
    this.storeManagementFacade.getStoreProductCategories(storeId).subscribe({
      next: (categories) => {
        console.log('Product categories received:', categories);
        this.productCategories = categories;
        this.productCategoriesDataSource.data = categories;
      },
      error: (error) => {
        console.error('Error loading product categories:', error);
        this.productCategories = [];
        this.productCategoriesDataSource.data = [];
      }
    });
  }

  getBack(): void {
    this.router.navigate(['../']);
  }

  editStore(): void {
    if (this.store) {
      this.router.navigate(['stores/edit', this.store.id]);
    }
  }

  manageCategories(): void {
    if (this.store) {
      // Open category management dialog
      // This could be implemented as a dialog or navigate to a separate page
    }
  }

  getStoreInitials(name: string): string {
    return name.charAt(0).toUpperCase();
  }

  getStoreBadgeClass(storeId: string): string {
    const badgeClasses = [
      'bg-light-primary text-primary',
      'bg-light-success text-success',
      'bg-light-warning text-warning',
      'bg-light-error text-error',
      'bg-light text-info'
    ];

    let hash = 5381;
    for (let i = 0; i < storeId.length; i++) {
      const char = storeId.charCodeAt(i);
      hash = ((hash << 5) + hash) + char;
    }

    const index = Math.abs(hash) % badgeClasses.length;
    return badgeClasses[index];
  }

  getStatusBadgeClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':
        return 'bg-light-success text-success';
      case 'INACTIVE':
        return 'bg-light-error text-error';
      case 'SUSPENDED':
        return 'bg-light-warning text-warning';
      case 'CLOSED':
        return 'bg-light-error text-error';
      default:
        return 'bg-light text-info';
    }
  }

  openManageCategoriesDialog(): void {
    // TODO: Implement category management dialog
    this.snackBar.open('Category management dialog coming soon!', 'Close', { duration: 3000 });
  }

  getCategoryStatusBadgeClass(isActive: boolean = true): string {
    return isActive ? 'badge bg-success' : 'badge bg-secondary';
  }

  getCategoryTypeBadgeClass(type: string): string {
    return 'badge bg-primary';
  }

  getCategoryPriority(category: StoreCategory): string {
    // For now, return a default priority. In a real app, this might be stored in the category model
    return 'Medium';
  }

  viewCategoryDetails(category: StoreCategory): void {
    // TODO: Implement category details view
    this.snackBar.open(`View details for ${category.name}`, 'Close', { duration: 3000 });
  }

  editCategory(category: StoreCategory): void {
    // TODO: Implement category editing
    this.snackBar.open(`Edit category ${category.name}`, 'Close', { duration: 3000 });
  }

  removeCategoryFromStore(category: StoreCategory): void {
    if (confirm(`Remove ${category.name} from this store?`)) {
      // TODO: Implement category removal from store
      this.snackBar.open(`Removed ${category.name} from store`, 'Close', { duration: 3000 });
      // Refresh categories list
      if (this.store) {
        this.loadStoreCategories(this.store.id);
      }
    }
  }

  createProductCategory(): void {
    // TODO: Implement create product category
    this.snackBar.open('Create Product Category - Coming Soon!', 'Close', { duration: 3000 });
  }

  viewProductCategory(category: ProductCategory): void {
    // TODO: Implement view product category
    this.snackBar.open(`View ${category.name} - Coming Soon!`, 'Close', { duration: 3000 });
  }

  editProductCategory(category: ProductCategory): void {
    // TODO: Implement edit product category
    this.snackBar.open(`Edit ${category.name} - Coming Soon!`, 'Close', { duration: 3000 });
  }

  deleteProductCategory(category: ProductCategory): void {
    if (confirm(`Delete ${category.name}? This action cannot be undone.`)) {
      // TODO: Implement delete product category
      this.snackBar.open(`Deleted ${category.name} - Coming Soon!`, 'Close', { duration: 3000 });
    }
  }
}