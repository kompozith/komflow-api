import { Component, Input, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { DatePipe } from '@angular/common';
import { StoreManagementFacade } from '../../../services/store-management.facade';
import { ProductCategory } from '../../../models/product-category';
import { CreateProductCategoryDialogComponent } from './create-product-category-dialog/create-product-category-dialog.component';
import { UpdateProductCategoryDialogComponent } from './update-product-category-dialog/update-product-category-dialog.component';
import { DeleteProductCategoryDialogComponent } from './delete-product-category-dialog/delete-product-category-dialog.component';

@Component({
  selector: 'app-product-categories',
  templateUrl: './product-categories.component.html',
  styleUrls: ['./product-categories.component.scss'],
  imports: [MaterialModule, TablerIconsModule, CommonModule, DatePipe, CreateProductCategoryDialogComponent, UpdateProductCategoryDialogComponent, DeleteProductCategoryDialogComponent],
})
export class ProductCategoriesComponent implements OnInit, AfterViewInit {
  @Input() storeId!: string;
  @Input() storeName!: string;

  productCategories: ProductCategory[] = [];
  productCategoriesDataSource = new MatTableDataSource<ProductCategory>();
  displayedColumns: string[] = ['position', 'name', 'productCount', 'createdAt', 'updatedAt', 'actions'];

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private storeManagementFacade: StoreManagementFacade,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    console.log('ProductCategoriesComponent initialized with storeId:', this.storeId, 'storeName:', this.storeName);
    this.loadProductCategories();
  }

  ngAfterViewInit(): void {
    // Ensure ViewChilds are initialized before setting data
    setTimeout(() => {
      this.productCategoriesDataSource.sort = this.sort;
      this.productCategoriesDataSource.paginator = this.paginator;
      // Re-assign data to ensure it's properly linked
      this.productCategoriesDataSource.data = this.productCategories;
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.productCategoriesDataSource.filter = filterValue.trim().toLowerCase();

    if (this.productCategoriesDataSource.paginator) {
      this.productCategoriesDataSource.paginator.firstPage();
    }
  }

  loadProductCategories(): void {
    this.storeManagementFacade.getStoreProductCategories(this.storeId).subscribe({
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

  createProductCategory(): void {
    const dialogRef = this.dialog.open(CreateProductCategoryDialogComponent, {
      width: '500px',
      data: { storeId: this.storeId, storeName: this.storeName }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && result.event === 'Create') {
        this.loadProductCategories();
      }
    });
  }

  viewProductCategory(category: ProductCategory): void {
    // TODO: Implement view product category
    this.snackBar.open(`View ${category.name} - Coming Soon!`, 'Close', { duration: 3000 });
  }

  editProductCategory(category: ProductCategory): void {
    const dialogRef = this.dialog.open(UpdateProductCategoryDialogComponent, {
      width: '500px',
      data: { category, storeId: this.storeId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && result.event === 'Update') {
        this.loadProductCategories();
      }
    });
  }

  deleteProductCategory(category: ProductCategory): void {
    const dialogRef = this.dialog.open(DeleteProductCategoryDialogComponent, {
      width: '500px',
      data: { category, storeId: this.storeId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.event === 'Delete') {
        this.loadProductCategories();
      }
    });
  }
}