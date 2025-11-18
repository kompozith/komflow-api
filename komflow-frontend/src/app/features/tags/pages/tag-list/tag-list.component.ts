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
import { TagService } from '../../services/tag.service';
import { Tag, TagPage, TagFilters } from '../../models/tag';
import { BadgeComponent, BadgeVariant } from '../../../../shared/components/badge/badge.component';
import { DeleteTagDialogComponent } from './delete-tag-dialog/delete-tag-dialog.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-tag-list',
  templateUrl: './tag-list.component.html',
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
export class TagListComponent implements OnInit, AfterViewInit {
  @ViewChild(MatTable, { static: true }) table: MatTable<any> =
    Object.create(null);

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;


  displayedColumns: string[] = [
    'rowNumber',
    'name',
    'color',
    'contactCount',
    'description',
    'createdAt',
    'actions',
  ];

  dataSource = new MatTableDataSource<Tag>([]);
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

  constructor(
      public dialog: MatDialog,
      private router: Router,
      private tagService: TagService,
      private snackBar: MatSnackBar
    ) {}

  ngOnInit(): void {
    this.loadTags();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadTags();
    });
  }

  ngAfterViewInit(): void {
    // No paginator setup needed for custom pagination
  }

  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  loadTags(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: TagFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      search: this.searchText || undefined,
    };

    this.tagService.getTags(filters).subscribe({
       next: (response: TagPage) => {
         this.dataSource.data = response.content;
         this.totalElements = response.totalElements;
         this.totalPages = response.totalPages;
         this.currentPage = response.number;
         this.pageSize = response.size;
         this.isLoading = false;

         // No paginator updates needed for custom pagination
       },
      error: (error) => {
        console.error('Error loading tags:', error);
        this.snackBar.open('Error loading tags', 'Close', { duration: 3000 });
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
      this.loadTags(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadTags(0); // Reset to first page when page size changes
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
    this.loadTags();
  }

  createTag(): void {
    this.router.navigate(['tags/create']);
  }

  editTag(tag: Tag): void {
    this.router.navigate(['tags/edit', tag.id]);
  }

  viewTagDetails(tag: Tag): void {
    this.router.navigate(['tags/details', tag.id]);
  }

  deleteTag(tag: Tag): void {
    const dialogRef = this.dialog.open(DeleteTagDialogComponent, {
      width: '500px',
      data: { tag }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.event === 'Delete') {
        this.loadTags(this.currentPage);
      }
    });
  }

  getColorPreview(colorCode?: string): string {
    return colorCode || '#007bff';
  }
}
