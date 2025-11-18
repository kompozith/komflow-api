import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ViewChild,
} from '@angular/core';
import { MatSort } from '@angular/material/sort';
import { PageEvent } from '@angular/material/paginator';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { FileService } from '../../services/file.service';
import { FileItem, FilePage, FileFilters, FileCategory } from '../../models/file';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { debounceTime, distinctUntilChanged, Subject, Subscription } from 'rxjs';

@Component({
  selector: 'app-files-list',
  templateUrl: './files-list.component.html',
  styleUrls: [],
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
export class FilesListComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild(MatSort) sort: MatSort | undefined;

  displayedColumns: string[] = [
    'rowNumber',
    'preview',
    'name',
    'category',
    'size',
    'uploadedBy',
    'uploadedAt',
    'actions',
  ];

  dataSource: FileItem[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  isLoading = false;

  // Filters
  searchText = '';
  selectedCategory: FileCategory | '' = '';
  sortBy = 'uploadedAt';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Search debounce
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  // Enums for template
  FileCategory = FileCategory;

  constructor(
    private router: Router,
    private fileService: FileService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadFiles();

    // Setup search debounce
    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadFiles();
    });
  }

  ngAfterViewInit(): void {
    if (this.sort) {
      this.sort.sortChange.subscribe(() => {
        this.sortBy = this.sort?.active || 'uploadedAt';
        this.sortDirection = this.sort?.direction || 'desc';
        this.loadFiles();
      });
    }
  }

  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  loadFiles(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: FileFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      search: this.searchText || undefined,
      category: this.selectedCategory || undefined,
    };

    this.fileService.getFiles(filters).subscribe({
       next: (response: FilePage) => {
          this.dataSource = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.currentPage = response.number;
          this.pageSize = response.size;
          this.isLoading = false;

          // No paginator updates needed for custom pagination
        },
      error: (error) => {
        console.error('Error loading files:', error);
        this.snackBar.open('Error loading files', 'Close', { duration: 3000 });
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
      this.loadFiles(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadFiles(0); // Reset to first page when page size changes
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onCategoryFilterChange(category: FileCategory | ''): void {
    this.selectedCategory = category;
    this.loadFiles();
  }

  onSortChange(sortBy: string): void {
    // This method is now handled by MatSort, but kept for backward compatibility
  }

  uploadFiles(): void {
    this.router.navigate(['files/upload']);
  }

  downloadFile(file: FileItem): void {
    this.fileService.downloadFile(file.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = file.originalName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        // Update download count
        file.downloadCount++;
      },
      error: (error) => {
        console.error('Error downloading file:', error);
        this.snackBar.open('Error downloading file', 'Close', { duration: 3000 });
      }
    });
  }

  deleteFile(file: FileItem): void {
    // Delete functionality not available - backend doesn't support file deletion
    this.snackBar.open('File deletion not supported', 'Close', { duration: 3000 });
    return;
    // const dialogRef = this.dialog.open(DeleteFileDialogComponent, {
    //   width: '500px',
    //   data: { file }
    // });

    // dialogRef.afterClosed().subscribe(result => {
    //   if (result?.event === 'Delete') {
    //     this.loadFiles(this.currentPage);
    //   }
    // });
  }

  getFileIcon(file: FileItem): string {
    return this.fileService.getFileIcon(file.mimeType);
  }

  formatFileSize(size: number): string {
    return this.fileService.formatFileSize(size);
  }

  isImageFile(file: FileItem): boolean {
    return this.fileService.isImageFile(file.mimeType);
  }

  getCategoryColor(category: FileCategory): 'primary' | 'success' | 'warning' | 'error' | 'outline' | 'info' {
    switch (category) {
      case FileCategory.IMAGE: return 'success';
      case FileCategory.VIDEO: return 'info';
      case FileCategory.AUDIO: return 'warning';
      case FileCategory.DOCUMENT: return 'primary';
      case FileCategory.ARCHIVE: return 'outline';
      default: return 'outline';
    }
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }
}
