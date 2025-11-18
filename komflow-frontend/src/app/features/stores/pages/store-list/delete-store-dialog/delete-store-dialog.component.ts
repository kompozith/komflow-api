import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
} from '@angular/material/dialog';
import { MaterialModule } from 'src/app/material.module';
import { FormsModule } from '@angular/forms';
import { TablerIconsModule } from 'angular-tabler-icons';
import { StoreManagementFacade } from '../../../services/store-management.facade';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Store } from '../../../models/store';

@Component({
  selector: 'app-delete-store-dialog',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MaterialModule,
    FormsModule,
    TablerIconsModule,
  ],
  templateUrl: './delete-store-dialog.component.html',
  styleUrl: './delete-store-dialog.component.scss',
})
export class DeleteStoreDialogComponent {
  action: string;
  local_data: any;
  confirmationText: string = '';

  constructor(
    public dialogRef: MatDialogRef<DeleteStoreDialogComponent>,
    private storeManagementFacade: StoreManagementFacade,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.action = 'Delete';
    this.local_data = { ...data.store };
  }

  get isConfirmationValid(): boolean {
    return this.confirmationText === this.local_data.name;
  }

  get store(): Store {
    return this.local_data;
  }

  doAction(): void {
    if (!this.isConfirmationValid) {
      return;
    }

    const storeId = this.local_data.id || this.data.id;
    this.storeManagementFacade.deleteStore(storeId).subscribe({
      next: () => {
        this.snackBar.open('Store deleted successfully!', 'Close', { duration: 3000 });
        this.dialogRef.close({ event: 'Delete' });
      },
      error: (error) => {
        console.error('Error deleting store:', error);
        this.snackBar.open('Error deleting store', 'Close', { duration: 3000 });
      }
    });
  }

  closeDialog(): void {
    this.dialogRef.close({ event: 'Cancel' });
  }
}