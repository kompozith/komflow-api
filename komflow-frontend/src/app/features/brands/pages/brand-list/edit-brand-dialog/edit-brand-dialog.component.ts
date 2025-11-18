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
import { Brand, BrandType } from '../../../models/brand';

@Component({
  selector: 'app-edit-brand-dialog',
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
  templateUrl: './edit-brand-dialog.component.html',
  styleUrl: './edit-brand-dialog.component.scss',
})
export class EditBrandDialogComponent {
  readonly dialogRef = inject(MatDialogRef<EditBrandDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA);

  brand: Brand = this.data.brand;

  nameControl = new FormControl(this.brand.name, [Validators.required, Validators.minLength(2)]);
  typeControl = new FormControl<BrandType>(this.brand.type, [Validators.required]);
  descriptionControl = new FormControl(this.brand.description || '');
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

  confirmUpdate() {
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

  hasChanges(): boolean {
    const nameChanged = this.nameControl.value !== this.brand.name;
    const typeChanged = this.typeControl.value !== this.brand.type;
    const descriptionChanged = this.descriptionControl.value !== (this.brand.description || '');
    const logoChanged = this.files.length > 0;

    return nameChanged || typeChanged || descriptionChanged || logoChanged;
  }
}