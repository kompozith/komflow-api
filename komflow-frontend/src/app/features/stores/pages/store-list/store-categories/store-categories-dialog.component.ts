import { Component, inject, OnInit } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MaterialModule } from 'src/app/material.module';
import { FormControl, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DatePipe } from '@angular/common';
import { Store } from '../../../models/store';
import { StoreCategory, StoreCategoryPage, StoreCategoryFilters } from '../../../models/store-category';
import { StoreManagementFacade } from '../../../services/store-management.facade';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-store-categories-dialog',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatDialogContent,
    MaterialModule,
    ReactiveFormsModule,
    FormsModule,
    DatePipe,
  ],
  templateUrl: './store-categories-dialog.component.html',
  styleUrl: './store-categories-dialog.component.scss',
})
export class StoreCategoriesDialogComponent implements OnInit {
  readonly dialogRef = inject(MatDialogRef<StoreCategoriesDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA);

  store: Store = this.data.store;

  categories: StoreCategory[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  isLoading = false;

  // Filters
  searchText = '';
  sortBy = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Search debounce
  private searchSubject = new Subject<string>();

  // New category form
  newCategoryNameControl = new FormControl('', [Validators.required, Validators.minLength(2)]);
  newCategoryDescriptionControl = new FormControl('');

  constructor(
    private storeManagementFacade: StoreManagementFacade,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCategories();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadCategories();
    });
  }

  loadCategories(pageIndex: number = 0): void {
    this.isLoading = true;

    const filters: StoreCategoryFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      search: this.searchText || undefined,
      storeId: this.store.id,
    };

    this.storeManagementFacade.getStoreCategories(this.store.id, filters).subscribe({
      next: (response: StoreCategoryPage) => {
        this.categories = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.number;
        this.pageSize = response.size;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.snackBar.open('Error loading categories', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  addCategory(): void {
    if (this.newCategoryNameControl.valid) {
      const categoryData = {
        name: this.newCategoryNameControl.value!,
        description: this.newCategoryDescriptionControl.value || undefined,
      };

      this.storeManagementFacade.createStoreCategory(this.store.id, categoryData).subscribe({
        next: (category) => {
          this.loadCategories();
          this.newCategoryNameControl.reset();
          this.newCategoryDescriptionControl.reset();
          this.snackBar.open('Category added successfully', 'Close', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error adding category:', error);
          this.snackBar.open('Error adding category', 'Close', { duration: 3000 });
        }
      });
    }
  }

  deleteCategory(category: StoreCategory): void {
    if (confirm(`Are you sure you want to delete the category "${category.name}"?`)) {
      this.storeManagementFacade.deleteStoreCategory(this.store.id, category.id).subscribe({
        next: () => {
          this.loadCategories();
          this.snackBar.open('Category deleted successfully', 'Close', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error deleting category:', error);
          this.snackBar.open('Error deleting category', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadCategories();
  }

  close(): void {
    this.dialogRef.close(true); // Indicate that categories may have been modified
  }
}