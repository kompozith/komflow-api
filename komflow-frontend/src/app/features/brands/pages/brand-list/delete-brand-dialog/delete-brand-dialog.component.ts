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
import { Brand } from '../../../models/brand';

@Component({
  selector: 'app-delete-brand-dialog',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatDialogContent,
    MaterialModule,
  ],
  templateUrl: './delete-brand-dialog.component.html',
  styleUrl: './delete-brand-dialog.component.scss',
})
export class DeleteBrandDialogComponent {
  readonly dialogRef = inject(MatDialogRef<DeleteBrandDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA);

  brand: Brand = this.data.brand;

  confirmDelete() {
    this.dialogRef.close('delete');
  }
}