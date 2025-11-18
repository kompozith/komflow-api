import {
  Component,
  ViewChild,
  AfterViewInit,
  OnInit,
} from '@angular/core';
import { MatTableDataSource, MatTable } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { PageEvent } from '@angular/material/paginator';
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { DatePipe } from '@angular/common';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrandService } from '../../services/brand.service';
import { Brand, BrandPage, BrandFilters, BrandType } from '../../models/brand';
import { BrandManagementFacade } from '../../services/brand-management.facade';
import { CreateBrandDialogComponent } from './create-brand-dialog/create-brand-dialog.component';
import { EditBrandDialogComponent } from './edit-brand-dialog/edit-brand-dialog.component';
import { DeleteBrandDialogComponent } from './delete-brand-dialog/delete-brand-dialog.component';
import { BadgeComponent, BadgeVariant } from '../../../../shared/components/badge/badge.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-brand-list',
  templateUrl: './brand-list.component.html',
  styleUrls: ['./brand-list.component.scss'],
  imports: [
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    TablerIconsModule,
    CommonModule,
    MatMenuModule,
    MatIconModule,
    BadgeComponent,
  ],
})
export class BrandListComponent implements OnInit, AfterViewInit {
  @ViewChild(MatTable, { static: true }) table: MatTable<any> =
    Object.create(null);

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;


  displayedColumns: string[] = [
    'rowNumber',
    'name',
    'type',
    'createdAt',
    'storeCount',
    'updatedAt',
    'actions',
  ];

  dataSource = new MatTableDataSource<Brand>([]);
   totalElements = 0;
   totalPages = 0;
   currentPage = 0;
   pageSize = 10;
   isLoading = false;


  // Filters
  searchText = '';
  selectedType: BrandType | '' = '';
  selectedHasStores: boolean | null = null;
  sortBy = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Search debounce
  private searchSubject = new Subject<string>();

  // Available filter options
  typeOptions: BrandType[] = ['SUPERMARKET', 'RESTAURANT', 'PHARMACY'];
  hasStoresOptions = [
    { value: null, label: 'All' },
    { value: true, label: 'With Stores' },
    { value: false, label: 'Without Stores' }
  ];

  constructor(
      public dialog: MatDialog,
      private brandService: BrandService,
      private brandManagementFacade: BrandManagementFacade,
      private snackBar: MatSnackBar
    ) {}

  ngOnInit(): void {
    this.loadBrands();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadBrands();
    });
  }

  ngAfterViewInit(): void {
    // No paginator setup needed for custom pagination
  }

  getBrandInitials(name: string): string {
    return name.charAt(0).toUpperCase();
  }

  getBrandBadgeClass(brandId: string): string {
    // Use badge-like classes with dark text and light backgrounds
    const badgeClasses = [
      'bg-light-primary text-primary',
      'bg-light-success text-success',
      'bg-light-warning text-warning',
      'bg-light-error text-error',
      'bg-light text-info'
    ];

    // Improved djb2 hash function for better distribution
    let hash = 5381;
    for (let i = 0; i < brandId.length; i++) {
      const char = brandId.charCodeAt(i);
      hash = ((hash << 5) + hash) + char; // hash * 33 + char
    }

    // Ensure positive index with better distribution
    const index = Math.abs(hash) % badgeClasses.length;
    return badgeClasses[index];
  }

  getTypeVariant(type: string): BadgeVariant {
    const typeVariants: Record<string, BadgeVariant> = {
      'SUPERMARKET': 'primary',
      'RESTAURANT': 'success',
      'PHARMACY': 'warning'
    };
    return typeVariants[type] || 'primary';
  }

  getStoreCountVariant(storeCount: number): BadgeVariant {
    return storeCount > 0 ? 'success' : 'error';
  }

  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  loadBrands(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: BrandFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      search: this.searchText || undefined,
      type: this.selectedType || undefined,
      hasStores: this.selectedHasStores ?? undefined,
    };

    this.brandManagementFacade.getBrands(filters).subscribe({
       next: (response: BrandPage) => {
         this.dataSource.data = response.content;
         this.totalElements = response.totalElements;
         this.totalPages = response.totalPages;
         this.currentPage = response.number;
         this.pageSize = response.size;
         this.isLoading = false;

         // No paginator updates needed for custom pagination
       },
      error: (error) => {
        console.error('Error loading brands:', error);
        this.snackBar.open('Error loading brands', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }


  onPageChange(event: PageEvent): void {
    const pageIndex = event.pageIndex;
    const newPageSize = event.pageSize;

    // Only reload if something actually changed
    if (pageIndex !== this.currentPage || newPageSize !== this.pageSize) {
      this.pageSize = newPageSize;
      this.loadBrands(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadBrands(0); // Reset to first page when page size changes
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onTypeFilterChange(type: string): void {
    this.selectedType = type as BrandType || '';
    this.loadBrands();
  }

  onHasStoresFilterChange(hasStores: string): void {
    this.selectedHasStores = hasStores === 'true' ? true : hasStores === 'false' ? false : null;
    this.loadBrands();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadBrands();
  }

  createBrand(): void {
    const dialogRef = this.dialog.open(CreateBrandDialogComponent, {
      data: {},
      width: '500px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.brandManagementFacade.createBrand(result).subscribe({
          next: (brand) => {
            this.loadBrands(0);
            this.snackBar.open('Brand created successfully', 'Close', { duration: 3000 });
          },
          error: (error) => {
            console.error('Error creating brand:', error);
            this.snackBar.open('Error creating brand', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  editBrand(brand: Brand): void {
    const dialogRef = this.dialog.open(EditBrandDialogComponent, {
      data: { brand },
      width: '500px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.brandManagementFacade.updateBrand(brand.id, result).subscribe({
          next: (updatedBrand) => {
            this.loadBrands(0);
            this.snackBar.open('Brand updated successfully', 'Close', { duration: 3000 });
          },
          error: (error) => {
            console.error('Error updating brand:', error);
            this.snackBar.open('Error updating brand', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  deleteBrand(brand: Brand): void {
    const dialogRef = this.dialog.open(DeleteBrandDialogComponent, {
      data: { brand },
      width: '400px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'delete') {
        this.brandManagementFacade.deleteBrand(brand.id).subscribe({
          next: () => {
            this.loadBrands(this.currentPage);
            this.snackBar.open('Brand deleted successfully!', 'Close', { duration: 3000 });
          },
          error: (error) => {
            console.error('Error deleting brand:', error);
            this.snackBar.open('Error deleting brand', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }
}