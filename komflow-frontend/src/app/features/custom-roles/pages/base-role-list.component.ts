import {
  Component,
  ViewChild,
  AfterViewInit,
  OnInit,
  ChangeDetectorRef,
} from '@angular/core';
import { MatTableDataSource, MatTable } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { PageEvent } from '@angular/material/paginator';
import {
  MatDialog,
} from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RoleService } from '../services/role.service';
import { Role, RolePage, RoleFilters } from '../models/role';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

export interface RoleListConfig {
  searchPlaceholder: string;
  title: string;
  showSystemRoles: boolean;
  showInactiveRoles: boolean;
}

export interface RoleDialog {
  id: string;
  name: string;
  displayName: string;
  description: string;
  isSystem: boolean;
  isActive: boolean;
  userCount: number;
  policyCount: number;
  createdByName: string;
  createdAt: string;
  updatedAt: string;
}

@Component({
  template: '', // Will be overridden by child components
})
export class BaseRoleListComponent implements OnInit, AfterViewInit {
  @ViewChild(MatTable, { static: true }) table: MatTable<any> =
    Object.create(null);

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;

  // Configuration - must be set by child components
  protected config!: RoleListConfig;

  displayedColumns: string[] = [
    'rowNumber',
    'name',
    'description',
    'type',
    'userCount',
    'policyCount',
    'status',
    'createdAt',
    'actions',
  ];

  dataSource = new MatTableDataSource<Role>([]);
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  isLoading = false;

  // Filters
  searchText = '';
  selectedStatus: 'active' | 'inactive' | '' = '';
  selectedType: 'system' | 'custom' | '' = '';
  sortBy = 'createdAt';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Search debounce
  protected searchSubject = new Subject<string>();

  constructor(
    protected dialog: MatDialog,
    protected roleService: RoleService,
    protected snackBar: MatSnackBar,
    protected cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadRoles();
    this.setupSearchDebounce();
  }

  ngAfterViewInit(): void {
    // Paginator setup if needed
  }

  private setupSearchDebounce(): void {
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadRoles();
    });
  }

  getRoleTypeBadgeClass(isSystem: boolean): string {
    return isSystem
      ? 'bg-light-error text-error'
      : 'bg-light-primary text-primary';
  }

  getRoleStatusBadgeClass(isActive: boolean): string {
    return isActive
      ? 'bg-light-success text-success rounded-pill f-w-600 p-6 p-y-4 f-s-12'
      : 'bg-light-warning text-warning rounded-pill f-w-600 p-6 p-y-4 f-s-12';
  }

  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  loadRoles(pageIndex: number = 0): void {
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: RoleFilters = {
      page: pageIndex,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection,
      search: this.searchText || undefined,
      isActive: this.selectedStatus === 'active' ? true :
                this.selectedStatus === 'inactive' ? false : undefined,
      isSystem: this.selectedType === 'system' ? true :
                this.selectedType === 'custom' ? false : undefined,
    };

    this.roleService.getRoles(filters).subscribe({
      next: (response: any) => {
        console.log('Raw API response:', response);

        // Try different possible response structures
        let data = [];
        if (response.content && Array.isArray(response.content)) {
          data = response.content;
        } else if (response.data && Array.isArray(response.data)) {
          data = response.data;
        } else if (Array.isArray(response)) {
          data = response;
        } else if (response.items && Array.isArray(response.items)) {
          data = response.items;
        }

        console.log('Extracted data:', data);
        console.log('Data length:', data.length);

        // Simple assignment
        console.log('Before assignment - dataSource.data.length:', this.dataSource.data.length);
        this.dataSource.data = data;
        this.totalElements = response.totalElements || response.total || data.length || 0;
        this.totalPages = response.totalPages || Math.ceil((response.totalElements || data.length || 0) / this.pageSize) || 0;
        this.currentPage = response.number || response.page || 0;
        this.pageSize = response.size || this.pageSize;

        console.log('After assignment:');
        console.log('- dataSource.data.length:', this.dataSource.data.length);
        console.log('- totalElements:', this.totalElements);
        console.log('- totalPages:', this.totalPages);
        console.log('- currentPage:', this.currentPage);
        console.log('- pageSize:', this.pageSize);

        this.isLoading = false;

        console.log('Final dataSource.data:', this.dataSource.data);

        // Force re-render
        setTimeout(() => {
          if (this.table) {
            this.table.renderRows();
          }
          this.cdr.detectChanges();
        }, 100);
      },
      error: (error) => {
        console.error('Error loading roles:', error);
        this.snackBar.open('Error loading roles', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    const pageIndex = event.pageIndex;
    const newPageSize = event.pageSize;

    console.log('Page change event:', event);
    console.log('Current pageSize:', this.pageSize, 'New pageSize:', newPageSize);

    // Check if page size changed - if so, reset to page 0
    const pageSizeChanged = newPageSize !== this.pageSize;
    const targetPage = pageSizeChanged ? 0 : pageIndex;

    if (pageSizeChanged || pageIndex !== this.currentPage) {
      this.pageSize = newPageSize;
      console.log('Loading roles with pageSize:', this.pageSize, 'page:', targetPage);

      // If page size changed, manually reset paginator to page 0
      if (pageSizeChanged && this.paginator) {
        this.paginator.pageIndex = 0;
        console.log('Reset paginator pageIndex to 0');
      }

      this.loadRoles(targetPage);
    }
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onStatusFilterChange(status: string): void {
    this.selectedStatus = status as 'active' | 'inactive' | '';
    this.loadRoles();
  }

  onTypeFilterChange(type: string): void {
    this.selectedType = type as 'system' | 'custom' | '';
    this.loadRoles();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadRoles();
  }

  openDialog(idOrIds: string | string[]): void {
    // To be implemented by child components
  }

  getDeletedById(id: string): void {
    this.roleService.deleteRole(id).subscribe({
      next: () => {
        this.loadRoles(this.currentPage);
        this.snackBar.open('Role deleted successfully!', 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error deleting role:', error);
        this.snackBar.open('Error deleting role', 'Close', { duration: 3000 });
      }
    });
  }

  deleteSelectedIds(ids: string[]): void {
    const deletePromises = ids.map(id => this.roleService.deleteRole(id).toPromise());

    Promise.all(deletePromises).then(() => {
      this.loadRoles(this.currentPage);
      this.snackBar.open(`${ids.length} roles deleted successfully!`, 'Close', { duration: 3000 });
    }).catch((error) => {
      console.error('Error deleting roles:', error);
      this.snackBar.open('Error deleting roles', 'Close', { duration: 3000 });
    });
  }
}