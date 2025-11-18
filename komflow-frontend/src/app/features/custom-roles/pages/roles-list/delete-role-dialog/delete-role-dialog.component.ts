import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { RoleService } from '../../../services/role.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-delete-role-dialog',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatDialogContent,
    MaterialModule,
    TablerIconsModule,
  ],
  templateUrl: './delete-role-dialog.component.html',
  styleUrl: './delete-role-dialog.component.scss',
})
export class DeleteRoleDialogComponent {
  action: string;
  local_data: any;

  constructor(
    public dialogRef: MatDialogRef<DeleteRoleDialogComponent>,
    private roleService: RoleService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.action = 'Delete';
    this.local_data = { ...data.role };
  }

  confirmDelete(): void {
    // Just close with confirmation, actual deletion will happen in the next dialog
    this.dialogRef.close({ event: 'ConfirmDelete', role: this.local_data });
  }

  closeDialog(): void {
    this.dialogRef.close({ event: 'Cancel' });
  }
}
