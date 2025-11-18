import {
  Component,
  Inject,
  Optional,
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
import { UserService } from '../services/user.service';
import { User, UserPage, UserFilters } from '../models/user';
import { USER_ROLES, USER_STATUSES, UserRole, UserStatus } from '../user-management.constants';
import { UserManagementFacade } from '../services/user-management.facade';
import { DeleteUserDialogComponent } from './user-list/delete-user-dialog/delete-user-dialog.component';
import { ChangeStatusDialogComponent } from './user-list/change-status-dialog/change-status-dialog.component';
import { BadgeComponent, BadgeVariant } from '../../../shared/components/badge/badge.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

export interface UserListConfig {
  userRole: UserRole | null; // null for all users, specific role for filtered lists
  searchPlaceholder: string;
  showRoleColumn: boolean;
  showRoleChangeAction: boolean;
  title: string;
}

export interface UserDialog {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: string;
  accountStatus: string;
  profilePicture: string;
}

@Component({
  template: '', // Will be overridden by child components
})
export class BaseUserListComponent implements OnInit, AfterViewInit {
  constructor(
    protected dialog: MatDialog,
    protected userService: UserService,
    protected userManagementFacade: UserManagementFacade,
    protected snackBar: MatSnackBar
  ) {}
  @ViewChild(MatTable, { static: true }) table: MatTable<any> =
    Object.create(null);

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;

  // Configuration - must be set by child components
  protected config!: UserListConfig;

  displayedColumns: string[] = [];

  dataSource = new MatTableDataSource<User>([]);
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  isLoading = false;

  // Filters
  searchText = '';
  selectedRole: UserRole | '' = '';
  selectedStatus: UserStatus | '' = '';
  registrationDateFrom: string | null = null;
  registrationDateTo: string | null = null;
  lastUpdatedFrom: string | null = null;
  lastUpdatedTo: string | null = null;
  sortBy = 'firstName';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Search debounce
  protected searchSubject = new Subject<string>();

  // Available filter options
  roleOptions: UserRole[] = Object.values(USER_ROLES);
  statusOptions: UserStatus[] = Object.values(USER_STATUSES);


  ngOnInit(): void {
    // Set up displayed columns based on configuration
    this.setupDisplayedColumns();

    this.loadUsers();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadUsers();
    });
  }

  ngAfterViewInit(): void {
    // No paginator setup needed for custom pagination
  }

  private setupDisplayedColumns(): void {
    const baseColumns = [
      'rowNumber',
      'name',
      'mobile',
      'registrationDate',
      'lastUpdated',
      'status',
      'actions',
    ];

    if (this.config.showRoleColumn) {
      // Insert role column after mobile
      baseColumns.splice(3, 0, 'role');
    }

    this.displayedColumns = baseColumns;
  }

  getUserInitials(firstName: string, lastName: string): string {
    return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
  }

  getUserBadgeClass(userId: string): string {
    const badgeClasses = [
      'bg-light-primary text-primary',
      'bg-light-success text-success',
      'bg-light-warning text-warning',
      'bg-light-error text-error',
      'bg-light-info text-info'
    ];

    let hash = 5381;
    for (let i = 0; i < userId.length; i++) {
      const char = userId.charCodeAt(i);
      hash = ((hash << 5) + hash) + char;
    }

    const index = Math.abs(hash) % badgeClasses.length;
    return badgeClasses[index];
  }

  getStatusVariant(status: string): BadgeVariant {
    const statusVariants: Record<string, BadgeVariant> = {
      'ACTIVE': 'success',
      'SUSPENDED': 'error',
      'PENDING': 'warning'
    };
    return statusVariants[status] || 'primary';
  }

  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  changeUserStatus(element: User): void {
    console.log('Opening status change dialog for user:', element);
    const dialogRef = this.dialog.open(ChangeStatusDialogComponent, {
      data: { user: element },
      width: '400px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      console.log('Status dialog result:', result);
      if (result !== undefined && result !== element.accountStatus) {
        console.log('Making API call to update status to:', result);
        this.userService.updateUserStatus(element.id, result).subscribe({
          next: (response) => {
            console.log('Status update response:', response);
            this.loadUsers(0);
            this.snackBar.open(`User status updated to ${result}`, 'Close', { duration: 3000 });
          },
          error: (error) => {
            console.error('Error updating user status:', error);
            this.snackBar.open('Error updating user status', 'Close', { duration: 3000 });
          }
        });
      } else {
        console.log('No status change or dialog cancelled');
      }
    });
  }

  changeUserRole(element: User): void {
    if (!this.config.showRoleChangeAction) {
      return; // Should not happen, but safety check
    }

    console.log('Opening role change dialog for user:', element);
    // Import would be needed here, but since this is base class, child components handle this
    // This method should be overridden in components that support role changes
  }

  loadUsers(pageIndex: number = 0): void {
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: UserFilters = {
      page: pageIndex,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection,
      search: this.searchText || undefined,
      role: this.config.userRole || this.selectedRole || undefined,
      status: this.selectedStatus || undefined,
      registrationDateFrom: this.registrationDateFrom || undefined,
      registrationDateTo: this.registrationDateTo || undefined,
      lastUpdatedFrom: this.lastUpdatedFrom || undefined,
      lastUpdatedTo: this.lastUpdatedTo || undefined,
    };

    this.userManagementFacade.getUsers(filters).subscribe({
      next: (response: UserPage) => {
        this.dataSource.data = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.number;
        this.pageSize = response.size;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.snackBar.open('Error loading users', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    const pageIndex = event.pageIndex;
    const newPageSize = event.pageSize;

    if (pageIndex !== this.currentPage || newPageSize !== this.pageSize) {
      this.pageSize = newPageSize;
      this.loadUsers(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadUsers(0);
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onRoleFilterChange(role: string): void {
    this.selectedRole = role as UserRole || '';
    this.loadUsers();
  }

  onStatusFilterChange(status: string): void {
    this.selectedStatus = status as UserStatus || '';
    this.loadUsers();
  }

  onDateFilterChange(): void {
    if (this.registrationDateFrom && typeof this.registrationDateFrom !== 'string') {
      this.registrationDateFrom = (this.registrationDateFrom as Date).toISOString().split('T')[0];
    }
    if (this.registrationDateTo && typeof this.registrationDateTo !== 'string') {
      this.registrationDateTo = (this.registrationDateTo as Date).toISOString().split('T')[0];
    }
    if (this.lastUpdatedFrom && typeof this.lastUpdatedFrom !== 'string') {
      this.lastUpdatedFrom = (this.lastUpdatedFrom as Date).toISOString().split('T')[0];
    }
    if (this.lastUpdatedTo && typeof this.lastUpdatedTo !== 'string') {
      this.lastUpdatedTo = (this.lastUpdatedTo as Date).toISOString().split('T')[0];
    }
    this.loadUsers();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadUsers();
  }

  openDialog(idOrIds: string | string[]): void {
    const dialogRef = this.dialog.open(DeleteUserDialogComponent, {
      data: {
        ids: Array.isArray(idOrIds) ? idOrIds : [idOrIds],
      },
      width: '400px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'delete') {
        if (Array.isArray(idOrIds)) {
          this.deleteSelectedIds(idOrIds);
        } else {
          this.getDeletedById(idOrIds);
        }
      }
    });
  }

  getDeletedById(id: string): void {
    this.userService.deleteUser(id).subscribe({
      next: () => {
        this.loadUsers(this.currentPage);
        this.snackBar.open('User deleted successfully!', 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error deleting user:', error);
        this.snackBar.open('Error deleting user', 'Close', { duration: 3000 });
      }
    });
  }

  deleteSelectedIds(ids: string[]): void {
    const deletePromises = ids.map(id => this.userService.deleteUser(id).toPromise());

    Promise.all(deletePromises).then(() => {
      this.loadUsers(this.currentPage);
      this.snackBar.open(`${ids.length} users deleted successfully!`, 'Close', { duration: 3000 });
    }).catch((error) => {
      console.error('Error deleting users:', error);
      this.snackBar.open('Error deleting users', 'Close', { duration: 3000 });
    });
  }
}