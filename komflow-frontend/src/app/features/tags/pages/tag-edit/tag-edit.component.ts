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
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { TagService } from '../../services/tag.service';
import { Tag, UpdateTagRequest } from '../../models/tag';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-tag-edit',
  imports: [
    MatDialogActions,
    MatDialogClose,
    MatDialogTitle,
    MatDialogContent,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    TablerIconsModule,
    CommonModule,
  ],
  templateUrl: './tag-edit.component.html',
  styleUrl: './tag-edit.component.scss',
})
export class TagEditComponent {
  tagForm: FormGroup;
  isLoading = false;
  isSaving = false;
  tag: Tag | null = null;

  // Predefined color options
  colorOptions = [
    { name: 'Blue', value: '#007bff' },
    { name: 'Green', value: '#28a745' },
    { name: 'Yellow', value: '#ffc107' },
    { name: 'Red', value: '#dc3545' },
    { name: 'Purple', value: '#6f42c1' },
    { name: 'Orange', value: '#fd7e14' },
    { name: 'Teal', value: '#20c997' },
    { name: 'Pink', value: '#e83e8c' },
    { name: 'Indigo', value: '#6610f2' },
    { name: 'Cyan', value: '#17a2b8' },
  ];

  constructor(
    public dialogRef: MatDialogRef<TagEditComponent>,
    private fb: FormBuilder,
    private tagService: TagService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { tag: Tag }
  ) {
    this.tag = data.tag;
    this.tagForm = this.fb.group({
      name: [data.tag.name, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      color: [data.tag.color || '#007bff'],
      description: [data.tag.description || '', [Validators.maxLength(255)]]
    });
  }

  onSubmit(): void {
    if (this.tagForm.valid) {
      this.isSaving = true;
      const formValue = this.tagForm.value;

      const tagData: UpdateTagRequest = {
        name: formValue.name,
        colorCode: formValue.color,
        description: formValue.description || undefined
      };

      this.tagService.updateTag(this.tag!.id.toString(), tagData).subscribe({
        next: (tag) => {
          this.snackBar.open('Tag updated successfully', 'Close', { duration: 3000 });
          this.dialogRef.close({ event: 'Update' });
        },
        error: (error) => {
          console.error('Error updating tag:', error);
          this.snackBar.open('Error updating tag', 'Close', { duration: 3000 });
          this.isSaving = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel(): void {
    this.dialogRef.close({ event: 'Cancel' });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.tagForm.controls).forEach(key => {
      const control = this.tagForm.get(key);
      control?.markAsTouched();
    });
  }
}
