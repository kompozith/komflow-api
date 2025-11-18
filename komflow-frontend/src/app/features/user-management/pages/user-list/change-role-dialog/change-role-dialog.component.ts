import { Component, inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MaterialModule } from 'src/app/material.module';
import { FormControl, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { User } from '../../../models/user';
import { USER_ROLES, UserRole } from '../../../user-management.constants';

@Component({
  selector: 'app-change-role-dialog',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatDialogContent,
    MaterialModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  templateUrl: './change-role-dialog.component.html',
  styleUrl: './change-role-dialog.component.scss',
})
export class ChangeRoleDialogComponent {
  readonly dialogRef = inject(MatDialogRef<ChangeRoleDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA);

  user: User = this.data.user;
  roleControl = new FormControl<UserRole>(this.user.role);
  reason: string = '';
  roleOptions: UserRole[] = Object.values(USER_ROLES);


  constructor() {
    console.log('ChangeRoleDialog initialized with user:', this.user);
    console.log('Current role:', this.roleControl.value);
  }

  confirmChange() {
    console.log('Confirming role change to:', this.roleControl.value, 'with reason:', this.reason);
    this.dialogRef.close({ newRole: this.roleControl.value, reason: this.reason });
  }
}
