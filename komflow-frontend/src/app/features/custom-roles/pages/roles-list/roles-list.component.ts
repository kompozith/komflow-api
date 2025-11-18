import {
  Component,
  Inject,
  Optional,
  ViewChild,
  AfterViewInit,
  OnInit,
} from '@angular/core';
import { MatTableDataSource, MatTable } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
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
import { RoleService } from '../../services/role.service';
import { Role, RolePage, RoleFilters } from '../../models/role';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { DeleteRoleDialogComponent } from './delete-role-dialog/delete-role-dialog.component';
import { ConfirmDeleteDialogComponent } from './confirm-delete-dialog/confirm-delete-dialog.component';
import { CreateRoleDialogComponent } from './create-role-dialog/create-role-dialog.component';
import { EditRoleDialogComponent } from './edit-role-dialog/edit-role-dialog.component';
import { BadgeComponent, BadgeVariant } from '../../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-roles-list',
  templateUrl: './roles-list.component.html',
  styleUrl: './roles-list.component.scss',
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
export class RolesListComponent implements OnInit, AfterViewInit {
  @ViewChild(MatTable, { static: true }) table: MatTable<any> =
    Object.create(null);

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;

  displayedColumns: string[] = [
    'rowNumber',
    'name',
    'type',
    'description',
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
  private searchSubject = new Subject<string>();

  constructor(
      public dialog: MatDialog,
      private roleService: RoleService,
      private snackBar: MatSnackBar,
      private router: Router
    ) {}

  ngOnInit(): void {
    this.loadRoles();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadRoles();
    });
  }

  ngAfterViewInit(): void {
    // No paginator setup needed for custom pagination
  }

  getRoleTypeVariant(isSystem: boolean): BadgeVariant {
    return isSystem ? 'info' : 'warning';
  }

  getRoleStatusVariant(isActive: boolean): BadgeVariant {
    return isActive ? 'success' : 'error';
  }

  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  createRole(): void {
    const dialogRef = this.dialog.open(CreateRoleDialogComponent, {
      width: '500px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.event === 'Create') {
        this.loadRoles();
      }
    });
  }

  viewRoleDetails(role: any): void {
    this.router.navigate(['/roles/details', role.id]);
  }

  editRole(role: any): void {
    const dialogRef = this.dialog.open(EditRoleDialogComponent, {
      data: { role },
      width: '500px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.event === 'Update') {
        this.loadRoles();
      }
    });
  }

  loadRoles(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
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
         console.log('Response type:', typeof response);
         console.log('Response keys:', response ? Object.keys(response) : 'null/undefined');

         // Try different response structures
         let data = [];
         if (response && response.content && Array.isArray(response.content)) {
           data = response.content;
         } else if (response && response.data && Array.isArray(response.data)) {
           data = response.data;
         } else if (Array.isArray(response)) {
           data = response;
         } else if (response && response.items && Array.isArray(response.items)) {
           data = response.items;
         }

         this.dataSource.data = data || [];
         this.totalElements = response?.totalElements || response?.total || (data ? data.length : 0) || 0;
         this.totalPages = response?.totalPages || Math.ceil(((response?.totalElements || (data ? data.length : 0) || 0)) / this.pageSize) || 0;
         this.currentPage = response?.number ?? response?.page ?? pageIndex ?? 0;
         this.pageSize = response?.size || this.pageSize;

         this.isLoading = false;
       },
      error: (error) => {
        console.error('Error loading roles:', error);
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);

        // If API fails, show empty table instead of crashing
        this.dataSource.data = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.currentPage = 0;
        this.isLoading = false;

        this.snackBar.open('Error loading roles. Please check the API endpoint.', 'Close', { duration: 5000 });
      }
    });
  }

  onPageChange(event: PageEvent): void {
    const pageIndex = event.pageIndex;
    const newPageSize = event.pageSize;

    // Only reload if something actually changed
    if (pageIndex !== this.currentPage || newPageSize !== this.pageSize) {
      this.pageSize = newPageSize;
      this.loadRoles(pageIndex);
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

  openDialog(roleOrId: any): void {
    // Handle both role object and ID string for backward compatibility
    const role = typeof roleOrId === 'string' ? null : roleOrId;
    const roleId = typeof roleOrId === 'string' ? roleOrId : roleOrId?.id;

    const dialogRef = this.dialog.open(DeleteRoleDialogComponent, {
      data: {
        role: role,
        id: roleId
      },
      width: '400px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.event === 'ConfirmDelete') {
        // Open the confirmation dialog
        this.openConfirmDeleteDialog(result.role);
      }
    });
  }

  openConfirmDeleteDialog(role: any): void {
    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      data: {
        role: role
      },
      width: '500px',
      enterAnimationDuration: '0ms',
      exitAnimationDuration: '0ms',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.event === 'Delete') {
        this.getDeletedById(role.id);
      }
    });
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