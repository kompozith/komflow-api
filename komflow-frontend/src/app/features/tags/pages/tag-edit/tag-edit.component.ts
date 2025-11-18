import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { TagService } from '../../services/tag.service';
import { Tag, UpdateTagRequest } from '../../models/tag';

@Component({
  selector: 'app-tag-edit',
  templateUrl: './tag-edit.component.html',
  styleUrls: [],
  imports: [
    MaterialModule,
    ReactiveFormsModule,
    FormsModule,
    TablerIconsModule,
    CommonModule,
  ],
})
export class TagEditComponent implements OnInit {
  tagForm: FormGroup;
  isLoading = false;
  isSaving = false;
  tagId: string = '';
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
    private fb: FormBuilder,
    private tagService: TagService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.tagForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      color: ['#007bff'],
      description: ['', [Validators.maxLength(255)]]
    });
  }

  ngOnInit(): void {
    this.tagId = this.route.snapshot.params['id'];
    this.loadTag();
  }

  loadTag(): void {
    this.isLoading = true;
    this.tagService.getTagById(this.tagId).subscribe({
      next: (tag) => {
        this.tag = tag;
        this.tagForm.patchValue({
          name: tag.name,
          color: tag.color || '#007bff',
          description: tag.description || ''
        });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading tag:', error);
        this.snackBar.open('Error loading tag', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.tagForm.valid) {
      this.isSaving = true;
      const formValue = this.tagForm.value;

      const tagData: UpdateTagRequest = {
        name: formValue.name,
        color: formValue.color,
        description: formValue.description || undefined
      };

      this.tagService.updateTag(this.tagId, tagData).subscribe({
        next: (tag) => {
          this.snackBar.open('Tag updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/tags/list']);
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
    this.router.navigate(['/tags/list']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.tagForm.controls).forEach(key => {
      const control = this.tagForm.get(key);
      control?.markAsTouched();
    });
  }
}
