import {
  Component,
  OnInit,
} from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import {
  MatDialog,
} from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuditService } from '../../services/audit.service';
import { AuditLog, AuditLogPage, AuditFilters, AuditAction } from '../../models/audit';
import {BadgeComponent, BadgeVariant} from '../../../../shared/components/badge/badge.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import {MatTableDataSource} from "@angular/material/table";

@Component({
  selector: 'app-audit-list',
  templateUrl: './audit-list.component.html',
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
export class AuditListComponent implements OnInit {


  displayedColumns: string[] = [
    'rowNumber',
    'timestamp',
    'username',
    'action',
    'resource',
    'success',
    'ipAddress',
    'details',
  ];

  dataSource = new MatTableDataSource<AuditLog>([]);
   totalElements = 0;
   totalPages = 0;
   currentPage = 0;
   pageSize = 10;
   isLoading = false;


  // Filters
  searchText = '';
  selectedAction: AuditAction | '' = '';
  selectedResource: string | '' = '';
  selectedSuccess: 'true' | 'false' | '' = '';
  dateFrom: string = '';
  dateTo: string = '';
  sortBy = 'timestamp';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Search debounce
  private searchSubject = new Subject<string>();

  // Enums for template
  AuditAction = AuditAction;

  constructor(
      public dialog: MatDialog,
      private router: Router,
      private auditService: AuditService,
      private snackBar: MatSnackBar
    ) {}

  ngOnInit(): void {
    this.loadAuditLogs();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadAuditLogs();
    });
  }


  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  loadAuditLogs(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: AuditFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      username: this.searchText || undefined,
      action: this.selectedAction || undefined,
      resource: this.selectedResource || undefined,
      success: this.selectedSuccess ? this.selectedSuccess === 'true' : undefined,
      dateFrom: this.dateFrom || undefined,
      dateTo: this.dateTo || undefined,
    };

    this.auditService.getAuditLogs(filters).subscribe({
       next: (response: AuditLogPage) => {
         this.dataSource.data = response.content;
         this.totalElements = response.totalElements;
         this.totalPages = response.totalPages;
         this.currentPage = response.number;
         this.pageSize = response.size;
         this.isLoading = false;

         // No paginator updates needed for custom pagination
       },
      error: (error) => {
        console.error('Error loading audit logs:', error);
        this.snackBar.open('Error loading audit logs', 'Close', { duration: 3000 });
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
      this.loadAuditLogs(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadAuditLogs(0); // Reset to first page when page size changes
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onActionFilterChange(action: AuditAction | ''): void {
    this.selectedAction = action;
    this.loadAuditLogs();
  }

  onResourceFilterChange(resource: string | ''): void {
    this.selectedResource = resource;
    this.loadAuditLogs();
  }

  onSuccessFilterChange(success: 'true' | 'false' | ''): void {
    this.selectedSuccess = success;
    this.loadAuditLogs();
  }

  onDateRangeChange(): void {
    this.loadAuditLogs();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadAuditLogs();
  }

  getActionColor(action: AuditAction): BadgeVariant {
    return this.auditService.getActionColor(action);
  }

  getActionIcon(action: AuditAction): string {
    return this.auditService.getActionIcon(action);
  }

  getResourceDisplayName(resource: string): string {
    return this.auditService.getResourceDisplayName(resource);
  }

  formatAuditValue(value: any): string {
    return this.auditService.formatAuditValue(value);
  }

  viewAuditDetails(auditLog: AuditLog): void {
    // TODO: Implement audit details modal or navigation
    this.snackBar.open('Audit details view coming soon!', 'Close', { duration: 3000 });
  }

  exportAuditLogs(): void {
    // TODO: Implement export functionality
    this.snackBar.open('Export functionality coming soon!', 'Close', { duration: 3000 });
  }
}
