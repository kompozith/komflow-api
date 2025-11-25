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
import { ContactService } from '../../services/contact.service';
import { Contact, ContactPage, ContactFilters } from '../../models/contact';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { DeleteContactDialogComponent } from './delete-contact-dialog/delete-contact-dialog.component';
import { ContactCreateComponent } from '../contact-create/contact-create.component';
import { ContactEditComponent } from '../contact-edit/contact-edit.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import {MatTableDataSource} from "@angular/material/table";

@Component({
  selector: 'app-contact-list',
  templateUrl: './contact-list.component.html',
  styleUrls: ['./contact-list.component.scss'],
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
export class ContactListComponent implements OnInit {


  displayedColumns: string[] = [
    'rowNumber',
    'name',
    'email',
    'phone',
    'tags',
    'createdAt',
    'actions',
  ];

  dataSource = new MatTableDataSource<Contact>([]);
   totalElements = 0;
   totalPages = 0;
   currentPage = 0;
   pageSize = 10;
   isLoading = false;


  // Filters
  searchText = '';
  selectedTagId: string | '' = '';
  sortBy = 'firstName';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Search debounce
  private searchSubject = new Subject<string>();

  constructor(
      public dialog: MatDialog,
      private router: Router,
      private contactService: ContactService,
      private snackBar: MatSnackBar
    ) {}

  ngOnInit(): void {
    this.loadContacts();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadContacts();
    });
  }


  getContactInitials(firstName: string, lastName: string): string {
    return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
  }

  getContactBadgeClass(contactId: string): string {
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
    for (let i = 0; i < contactId.length; i++) {
      const char = contactId.charCodeAt(i);
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

  loadContacts(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: ContactFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      search: this.searchText || undefined,
      tagId: this.selectedTagId || undefined,
    };

    this.contactService.getContacts(filters).subscribe({
       next: (response: ContactPage) => {
         this.dataSource.data = response.content;
         this.totalElements = response.totalElements;
         this.totalPages = response.totalPages;
         this.currentPage = response.number;
         this.pageSize = response.size;
         this.isLoading = false;

         // No paginator updates needed for custom pagination
       },
      error: (error) => {
        console.error('Error loading contacts:', error);
        this.snackBar.open('Error loading contacts', 'Close', { duration: 3000 });
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
      this.loadContacts(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadContacts(0); // Reset to first page when page size changes
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onTagFilterChange(tagId: string): void {
    this.selectedTagId = tagId || '';
    this.loadContacts();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadContacts();
  }

  createContact(): void {
    const dialogRef = this.dialog.open(ContactCreateComponent, {
      width: '600px',
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.event === 'Create') {
        this.loadContacts(this.currentPage);
      }
    });
  }

  editContact(contact: Contact): void {
    this.router.navigate(['contacts/edit', contact.id]);
  }

  viewContactDetails(contact: Contact): void {
    this.router.navigate(['contacts/details', contact.id]);
  }

  deleteContact(contact: Contact): void {
    const dialogRef = this.dialog.open(DeleteContactDialogComponent, {
      width: '500px',
      data: { contact }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.event === 'Delete') {
        this.loadContacts(this.currentPage);
      }
    });
  }
}
