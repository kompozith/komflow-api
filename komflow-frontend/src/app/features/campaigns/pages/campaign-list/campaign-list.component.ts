import {
  Component,
  OnInit,
} from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import {
  MatDialog,
} from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { CampaignService } from '../../services/campaign.service';
import { Campaign, CampaignPage, CampaignFilters, CampaignStatus } from '../../models/campaign';
import {BadgeComponent, BadgeVariant} from '../../../../shared/components/badge/badge.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-campaign-list',
  templateUrl: './campaign-list.component.html',
  styleUrls: [],
  imports: [
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    TablerIconsModule,
    CommonModule,
    BadgeComponent,
  ],
})
export class CampaignListComponent implements OnInit {


  displayedColumns: string[] = [
    'rowNumber',
    'name',
    'message',
    'status',
    'recipients',
    'sentAt',
    'createdAt',
    'actions',
  ];

  dataSource: Campaign[] = [];
   totalElements = 0;
   totalPages = 0;
   currentPage = 0;
   pageSize = 10;
   isLoading = false;


  // Filters
  searchText = '';
  selectedStatus: CampaignStatus | '' = '';
  selectedMessageId: string | '' = '';
  sortBy = 'createdAt';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Search debounce
  private searchSubject = new Subject<string>();

  // Enums for template
  CampaignStatus = CampaignStatus;

  constructor(
      public dialog: MatDialog,
      private router: Router,
      private campaignService: CampaignService,
      private snackBar: MatSnackBar
    ) {}

  ngOnInit(): void {
    this.loadCampaigns();

    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchText = searchText;
      this.loadCampaigns();
    });
  }


  getStartIndex(): number {
    return (this.currentPage * this.pageSize) + 1;
  }

  getEndIndex(): number {
    const endIndex = (this.currentPage + 1) * this.pageSize;
    return Math.min(endIndex, this.totalElements);
  }

  loadCampaigns(pageIndex: number = 0): void {
    // Prevent multiple simultaneous requests
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;

    const filters: CampaignFilters = {
      page: pageIndex,
      size: this.pageSize,
      sort: [`${this.sortBy},${this.sortDirection}`],
      search: this.searchText || undefined,
      status: this.selectedStatus || undefined,
      messageId: this.selectedMessageId || undefined,
    };

    this.campaignService.getCampaigns(filters).subscribe({
       next: (response: CampaignPage) => {
          this.dataSource = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.currentPage = response.number;
          this.pageSize = response.size;
          this.isLoading = false;

          // No paginator updates needed for custom pagination
        },
      error: (error) => {
        console.error('Error loading campaigns:', error);
        this.snackBar.open('Error loading campaigns', 'Close', { duration: 3000 });
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
      this.loadCampaigns(pageIndex);
    }
  }

  onPageSizeChange(newPageSize: number): void {
    this.pageSize = newPageSize;
    this.loadCampaigns(0); // Reset to first page when page size changes
  }

  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }

  onStatusFilterChange(status: CampaignStatus | ''): void {
    this.selectedStatus = status;
    this.loadCampaigns();
  }

  onMessageFilterChange(messageId: string | ''): void {
    this.selectedMessageId = messageId;
    this.loadCampaigns();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'asc';
    }
    this.loadCampaigns();
  }

  createCampaign(): void {
    this.router.navigate(['campaigns/create']);
  }

  editCampaign(campaign: Campaign): void {
    this.router.navigate(['campaigns/edit', campaign.id]);
  }

  viewCampaignDetails(campaign: Campaign): void {
    this.router.navigate(['campaigns/details', campaign.id]);
  }

  deleteCampaign(campaign: Campaign): void {
    // Delete functionality not available - backend doesn't support campaign deletion
    this.snackBar.open('Campaign deletion not supported', 'Close', { duration: 3000 });
    return;
    // const dialogRef = this.dialog.open(DeleteCampaignDialogComponent, {
    //   width: '500px',
    //   data: { campaign }
    // });

    // dialogRef.afterClosed().subscribe(result => {
    //   if (result?.event === 'Delete') {
    //     this.loadCampaigns(this.currentPage);
    //   }
    // });
  }

  sendCampaign(campaign: Campaign): void {
    if (confirm(`Are you sure you want to send the campaign "${campaign.name}"? This action cannot be undone.`)) {
      this.campaignService.sendCampaign({ campaignId: campaign.id }).subscribe({
        next: (updatedCampaign) => {
          this.snackBar.open('Campaign sent successfully', 'Close', { duration: 3000 });
          this.loadCampaigns(this.currentPage);
        },
        error: (error) => {
          console.error('Error sending campaign:', error);
          this.snackBar.open('Error sending campaign', 'Close', { duration: 3000 });
        }
      });
    }
  }

  cancelCampaign(campaign: Campaign): void {
    if (confirm(`Are you sure you want to cancel the campaign "${campaign.name}"?`)) {
      this.campaignService.cancelCampaign(campaign.id).subscribe({
        next: (updatedCampaign) => {
          this.snackBar.open('Campaign cancelled successfully', 'Close', { duration: 3000 });
          this.loadCampaigns(this.currentPage);
        },
        error: (error) => {
          console.error('Error cancelling campaign:', error);
          this.snackBar.open('Error cancelling campaign', 'Close', { duration: 3000 });
        }
      });
    }
  }

  getStatusColor(status: CampaignStatus): BadgeVariant {
    switch (status) {
      case CampaignStatus.DRAFT: return 'secondary';
      case CampaignStatus.SCHEDULED: return 'info';
      case CampaignStatus.RUNNING: return 'warning';
      case CampaignStatus.COMPLETED: return 'success';
      case CampaignStatus.CANCELLED: return 'outline';
      case CampaignStatus.FAILED: return 'error';
      default: return 'outline';
    }
  }

  canSendCampaign(campaign: Campaign): boolean {
    return campaign.status === CampaignStatus.DRAFT || campaign.status === CampaignStatus.SCHEDULED;
  }

  canCancelCampaign(campaign: Campaign): boolean {
    return campaign.status === CampaignStatus.SCHEDULED || campaign.status === CampaignStatus.RUNNING;
  }
}
