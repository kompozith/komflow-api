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
import { FormsModule } from '@angular/forms';
import { TablerIconsModule } from 'angular-tabler-icons';
import { RoleService } from '../../../services/role.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-create-role-dialog',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatDialogContent,
    MaterialModule,
    FormsModule,
    TablerIconsModule,
  ],
  templateUrl: './create-role-dialog.component.html',
  styleUrl: './create-role-dialog.component.scss',
})
export class CreateRoleDialogComponent {
  action: string;
  local_data: any;

  constructor(
    public dialogRef: MatDialogRef<CreateRoleDialogComponent>,
    private roleService: RoleService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.action = 'Add';
    this.local_data = { name: '', displayName: '', description: '' };
  }

  doAction(): void {
    const roleData = {
      name: this.local_data.name,
      displayName: this.local_data.displayName,
      description: this.local_data.description || '',
    };

    this.roleService.createRole(roleData).subscribe({
      next: (response) => {
        this.snackBar.open('Role created successfully!', 'Close', { duration: 3000 });
        this.dialogRef.close({ event: 'Create' });
      },
      error: (error) => {
        console.error('Error creating role:', error);
        this.snackBar.open('Error creating role', 'Close', { duration: 3000 });
      }
    });
  }

  closeDialog(): void {
    this.dialogRef.close({ event: 'Cancel' });
  }
}