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
import { Router } from '@angular/router';
import { StoreService } from '../../services/store.service';
import { Store, StorePage, StoreFilters } from '../../models/store';
import { StoreManagementFacade } from '../../services/store-management.facade';
import { BadgeComponent, BadgeVariant } from '../../../../shared/components/badge/badge.component';
import { DeleteStoreDialogComponent } from './delete-store-dialog/delete-store-dialog.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-store-list',
  templateUrl: './store-list.component.html',
  styleUrls: ['./store-list.component.scss'],
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
export class StoreListComponent implements OnInit, AfterViewInit {
  @ViewChild(MatTable, { static: true }) table: MatTable<any> =
    Object.create(null);

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;


  displayedColumns: string[] = [
    'rowNumber',
    'name',
    'brand',
    'productCount',
    'address',
    'phone',
    'createdAt',
    'actions',
  ];

  dataSource = new MatTableDataSource<Store>([]);
   totalElements = 0;
   totalPages = 0;
   currentPage = 0;
   pageSize = 10;
   isLoading = false;


  // Filters
  searchText = '';
  selectedBrandId: string | '' = '';
  selectedCity: string | '' = '';
  selectedRegion: string | '' = '';
  sortBy = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Search debounce
  private searchSubject = new Subject<string>();

  constructor(
      public dialog: MatDialog,
      private router: Router,
      private storeService: StoreService,
      private storeManagementFacade: StoreManagementFacade,
      private snackBar: MatSnackBar
    ) {}

  ngOnInit(): void {
    this.loadStores();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadStores();
    });
  }

  ngAfterViewInit(): void {
    // No paginator setup needed for custom pagination
  }

  getStoreInitials(name: string): string {
    return name.charAt(0).toUpperCase();
  }

  getStoreBadgeClass(storeId: string): string {
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
    for (let i = 0; i < storeId.length; i++) {
      const char = storeId.charCodeAt(i);
      hash = ((hash << 5) + hash) + char; // hash * 33 + char
    }

    // Ensure positive index with better distribution
    const index = Math.abs(hash) % badgeClasses.length;
    return badgeClasses[index];
  }

  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  loadStores(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: StoreFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      search: this.searchText || undefined,
      brandId: this.selectedBrandId || undefined,
      city: this.selectedCity || undefined,
      region: this.selectedRegion || undefined,
    };

    this.storeManagementFacade.getStores(filters).subscribe({
       next: (response: StorePage) => {
         this.dataSource.data = response.content;
         this.totalElements = response.totalElements;
         this.totalPages = response.totalPages;
         this.currentPage = response.number;
         this.pageSize = response.size;
         this.isLoading = false;

         // No paginator updates needed for custom pagination
       },
      error: (error) => {
        console.error('Error loading stores:', error);
        this.snackBar.open('Error loading stores', 'Close', { duration: 3000 });
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
      this.loadStores(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadStores(0); // Reset to first page when page size changes
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onBrandFilterChange(brandId: string): void {
    this.selectedBrandId = brandId || '';
    this.loadStores();
  }

  onCityFilterChange(city: string): void {
    this.selectedCity = city || '';
    this.loadStores();
  }

  onRegionFilterChange(region: string): void {
    this.selectedRegion = region || '';
    this.loadStores();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadStores();
  }

  createStore(): void {
    this.router.navigate(['stores/create']);
  }

  editStore(store: Store): void {
    this.router.navigate(['stores/edit', store.id]);
  }

  viewStoreDetails(store: Store): void {
    this.router.navigate(['stores/details', store.id]);
  }

  deleteStore(store: Store): void {
    const dialogRef = this.dialog.open(DeleteStoreDialogComponent, {
      width: '500px',
      data: { store }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.event === 'Delete') {
        this.loadStores(this.currentPage);
      }
    });
  }

  manageCategories(store: Store): void {
    // TODO: Implement category management - could navigate to a separate page or open a dialog
    this.snackBar.open('Category management coming soon!', 'Close', { duration: 3000 });
  }
}