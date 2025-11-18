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
import { FormControl, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { NgxDropzoneModule } from 'ngx-dropzone';
import { BrandType } from '../../../models/brand';

@Component({
  selector: 'app-create-brand-dialog',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatDialogContent,
    MaterialModule,
    ReactiveFormsModule,
    FormsModule,
    NgxDropzoneModule,
  ],
  templateUrl: './create-brand-dialog.component.html',
  styleUrl: './create-brand-dialog.component.scss',
})
export class CreateBrandDialogComponent {
  readonly dialogRef = inject(MatDialogRef<CreateBrandDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA);

  nameControl = new FormControl('', [Validators.required, Validators.minLength(2)]);
  typeControl = new FormControl<BrandType>('SUPERMARKET', [Validators.required]);
  descriptionControl = new FormControl('');
  files: File[] = [];

  typeOptions: BrandType[] = ['SUPERMARKET', 'RESTAURANT', 'PHARMACY'];

  constructor() {}

  onSelect(event: any) {
    const files = event.addedFiles; // Getting the selected files

    // Since we set multiple="false", we only take the first file
    if (files && files.length > 0) {
      this.files = [files[0]]; // Replace any existing file
    }
  }

  // Method to remove file
  onRemove(file: any) {
    this.files = this.files.filter(f => f !== file);
  }

  confirmCreate() {
    if (this.nameControl.valid && this.typeControl.valid) {
      const brandData = {
        name: this.nameControl.value!,
        type: this.typeControl.value!,
        description: this.descriptionControl.value || undefined,
        logo: this.files.length > 0 ? this.files[0] : undefined,
      };
      this.dialogRef.close(brandData);
    }
  }

  isFormValid(): boolean {
    return this.nameControl.valid && this.typeControl.valid;
  }
}