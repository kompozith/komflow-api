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
import { MessageService } from '../../services/message.service';
import { Message, MessagePage, MessageFilters, MessageChannel, MessageType } from '../../models/message';
import { BadgeComponent, BadgeVariant } from '../../../../shared/components/badge/badge.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-message-list',
  templateUrl: './message-list.component.html',
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
export class MessageListComponent implements OnInit, AfterViewInit {
  @ViewChild(MatTable, { static: true }) table: MatTable<any> =
    Object.create(null);

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;


  displayedColumns: string[] = [
    'rowNumber',
    'name',
    'channel',
    'type',
    'status',
    'variablesCount',
    'createdAt',
    'actions',
  ];

  dataSource = new MatTableDataSource<Message>([]);
   totalElements = 0;
   totalPages = 0;
   currentPage = 0;
   pageSize = 10;
   isLoading = false;


  // Filters
  searchText = '';
  selectedChannel: MessageChannel | '' = '';
  selectedType: MessageType | '' = '';
  selectedStatus: 'ACTIVE' | 'INACTIVE' | '' = '';
  sortBy = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Search debounce
  private searchSubject = new Subject<string>();

  // Enums for template
  MessageChannel = MessageChannel;
  MessageType = MessageType;

  constructor(
      public dialog: MatDialog,
      private router: Router,
      private messageService: MessageService,
      private snackBar: MatSnackBar
    ) {}

  ngOnInit(): void {
    this.loadMessages();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadMessages();
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

  loadMessages(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: MessageFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      search: this.searchText || undefined,
      channel: this.selectedChannel || undefined,
      type: this.selectedType || undefined,
      status: this.selectedStatus || undefined,
    };

    this.messageService.getMessages(filters).subscribe({
       next: (response: MessagePage) => {
         this.dataSource.data = response.content;
         this.totalElements = response.totalElements;
         this.totalPages = response.totalPages;
         this.currentPage = response.number;
         this.pageSize = response.size;
         this.isLoading = false;

         // No paginator updates needed for custom pagination
       },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.snackBar.open('Error loading messages', 'Close', { duration: 3000 });
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
      this.loadMessages(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadMessages(0); // Reset to first page when page size changes
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onChannelFilterChange(channel: MessageChannel | ''): void {
    this.selectedChannel = channel;
    this.loadMessages();
  }

  onTypeFilterChange(type: MessageType | ''): void {
    this.selectedType = type;
    this.loadMessages();
  }

  onStatusFilterChange(status: 'ACTIVE' | 'INACTIVE' | ''): void {
    this.selectedStatus = status;
    this.loadMessages();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadMessages();
  }

  createMessage(): void {
    this.router.navigate(['messages/create']);
  }

  // Note: Edit and details views not available - only create and list

  deleteMessage(message: Message): void {
    // Delete functionality not available - backend doesn't support message deletion
    this.snackBar.open('Message deletion not supported', 'Close', { duration: 3000 });
    return;
  }

  getChannelIcon(channel: MessageChannel): string {
    switch (channel) {
      case MessageChannel.EMAIL: return 'mail';
      case MessageChannel.SMS: return 'message-circle';
      case MessageChannel.WHATSAPP: return 'brand-whatsapp';
      default: return 'message';
    }
  }

  getChannelColor(channel: MessageChannel): BadgeVariant {
    switch (channel) {
      case MessageChannel.EMAIL: return 'info';
      case MessageChannel.SMS: return 'success';
      case MessageChannel.WHATSAPP: return 'warning';
      default: return 'primary';
    }
  }

  getTypeColor(type: MessageType): BadgeVariant {
    switch (type) {
      case MessageType.MARKETING: return 'info';
      case MessageType.TRANSACTIONAL: return 'success';
      case MessageType.NOTIFICATION: return 'warning';
      default: return 'primary';
    }
  }
}
