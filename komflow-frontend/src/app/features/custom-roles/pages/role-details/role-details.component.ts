import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { RoleService } from '../../services/role.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ManagePoliciesDialogComponent } from './manage-policies-dialog/manage-policies-dialog.component';

interface Policy {
  id: string;
  name: string;
  description: string;
  policyRule: string;
  type: 'ALLOW' | 'DENY';
  resource: string;
  action: string;
  priority: number;
  isActive: boolean;
  conditionData: string;
  createdAt: string;
  updatedAt: string;
}

interface RoleDetails {
  id: string;
  name: string;
  displayName: string;
  description: string;
  isSystem: boolean;
  isActive: boolean;
  userCount: number;
  policies: Policy[];
}

@Component({
  selector: 'app-role-details',
  imports: [
    MaterialModule,
    TablerIconsModule,
    CommonModule,
  ],
  templateUrl: './role-details.component.html',
})
export class RoleDetailsComponent implements OnInit {
  roleId: string = '';
  role: RoleDetails | null = null;
  isLoading = true;

  displayedPolicyColumns: string[] = [
    'name',
    'type',
    'resource',
    'action',
    'priority',
    'status',
    'createdAt'
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private roleService: RoleService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.roleId = this.route.snapshot.params['id'];
    if (this.roleId) {
      this.loadRoleDetails();
    } else {
      this.snackBar.open('Role ID not found', 'Close', { duration: 3000 });
      this.router.navigate(['/custom-roles']);
    }
  }

  loadRoleDetails(): void {
    this.isLoading = true;
    this.roleService.getRoleById(this.roleId).subscribe({
      next: (role: RoleDetails) => {
        this.role = role;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading role details:', error);
        this.snackBar.open('Error loading role details', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.router.navigate(['/roles/list']);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/roles/list']);
  }

  getRoleTypeBadgeClass(isSystem: boolean): string {
    return isSystem
      ? 'bg-light-danger text-danger rounded-pill f-w-600 p-6 p-y-4 f-s-12'
      : 'bg-light-primary text-primary rounded-pill f-w-600 p-6 p-y-4 f-s-12';
  }

  getRoleStatusBadgeClass(isActive: boolean): string {
    return isActive
      ? 'bg-light-success text-success rounded-pill f-w-600 p-6 p-y-4 f-s-12'
      : 'bg-light-error text-error rounded-pill f-w-600 p-6 p-y-4 f-s-12';
  }

  getPolicyTypeBadgeClass(type: string): string {
    return type === 'ALLOW'
      ? 'bg-light-success text-success rounded-pill f-w-600 p-6 p-y-4 f-s-12'
      : 'bg-light-error text-error rounded-pill f-w-600 p-6 p-y-4 f-s-12';
  }

  getPolicyStatusBadgeClass(isActive: boolean): string {
    return isActive
      ? 'bg-light-success text-success rounded-pill f-w-600 p-6 p-y-4 f-s-12'
      : 'bg-light-error text-error rounded-pill f-w-600 p-6 p-y-4 f-s-12';
  }

  getActivePoliciesCount(): number {
    return this.role?.policies?.filter(p => p.isActive)?.length || 0;
  }

  openManagePoliciesDialog(): void {
    if (!this.role) return;

    const dialogRef = this.dialog.open(ManagePoliciesDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: {
        roleId: this.role.id,
        currentPolicies: this.role.policies || []
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Refresh role details after policies are updated
        this.loadRoleDetails();
      }
    });
  }
}