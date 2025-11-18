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
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { User } from '../../../models/user';

@Component({
  selector: 'app-change-status-dialog',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatDialogContent,
    MaterialModule,
    ReactiveFormsModule,
  ],
  templateUrl: './change-status-dialog.component.html',
  styleUrl: './change-status-dialog.component.scss',
})
export class ChangeStatusDialogComponent {
  readonly dialogRef = inject(MatDialogRef<ChangeStatusDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA);

  user: User = this.data.user;
  statusControl = new FormControl<User['accountStatus']>(this.user.accountStatus);

  statusOptions: User['accountStatus'][] = ['ACTIVE', 'SUSPENDED', 'PENDING'];

  constructor() {
    console.log('ChangeStatusDialog initialized with user:', this.user);
    console.log('Current status:', this.statusControl.value);
  }

  confirmChange() {
    console.log('Confirming status change to:', this.statusControl.value);
    this.dialogRef.close(this.statusControl.value);
  }
}
