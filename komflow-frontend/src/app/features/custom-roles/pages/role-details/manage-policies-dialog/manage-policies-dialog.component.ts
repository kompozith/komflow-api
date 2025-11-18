import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { CustomRolesFacade } from '../../../services/custom-roles.facade';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Policy } from '../../../models/role';
import { BadgeComponent } from '../../../../../shared/components/badge/badge.component';

interface DialogData {
  roleId: string;
  currentPolicies: Policy[];
}

@Component({
  selector: 'app-manage-policies-dialog',
  imports: [
    MaterialModule,
    TablerIconsModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BadgeComponent,
  ],
  templateUrl: './manage-policies-dialog.component.html',
})
export class ManagePoliciesDialogComponent implements OnInit {
  allPolicies: Policy[] = [];
  selectedPolicyIds: string[] = [];
  isLoading = false;
  isSubmitting = false;

  policySelectControl = new FormControl<string[]>([]);

  constructor(
    public dialogRef: MatDialogRef<ManagePoliciesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private customRolesFacade: CustomRolesFacade,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.selectedPolicyIds = this.data.currentPolicies.map(p => p.id);
    this.policySelectControl.setValue(this.selectedPolicyIds);
    this.loadPolicies();
  }

  loadPolicies(): void {
    this.isLoading = true;
    this.customRolesFacade.getPolicies().subscribe({
      next: (policies: Policy[]) => {
        this.allPolicies = policies;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading policies:', error);
        this.snackBar.open('Error loading policies', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    const selectedIds = this.policySelectControl.value || [];
    if (selectedIds.length === 0) {
      this.snackBar.open('Please select at least one policy', 'Close', { duration: 3000 });
      return;
    }

    this.isSubmitting = true;
    this.customRolesFacade.assignPoliciesToRole(this.data.roleId, selectedIds).subscribe({
      next: () => {
        this.snackBar.open('Policies assigned successfully', 'Close', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (error: any) => {
        console.error('Error assigning policies:', error);
        this.snackBar.open('Error assigning policies', 'Close', { duration: 3000 });
        this.isSubmitting = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  comparePolicies(policyId1: string, policyId2: string): boolean {
    return policyId1 === policyId2;
  }

  getPolicyById(policyId: string): Policy | undefined {
    return this.allPolicies.find(p => p.id === policyId);
  }

  removePolicy(policyId: string): void {
    const currentValues = this.policySelectControl.value || [];
    const updatedValues = currentValues.filter(id => id !== policyId);
    this.policySelectControl.setValue(updatedValues);
  }

  getPolicyDisplayName(policy: Policy | undefined): string {
    if (!policy) return '';
    return `${policy.name} (${policy.resource}:${policy.action})`;
  }
}