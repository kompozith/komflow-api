import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MaterialModule } from 'src/app/material.module';
import { TagService } from '../../../services/tag.service';
import { Tag } from '../../../models/tag';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-delete-tag-dialog',
  template: `
    <h2 mat-dialog-title>Delete Tag</h2>
    <mat-dialog-content>
      <p>Are you sure you want to delete the tag <strong>{{ data.tag.name }}</strong>?</p>
      <p class="text-muted">This action cannot be undone. The tag will be removed from all associated contacts.</p>
      <p *ngIf="data.tag.contactCount && data.tag.contactCount > 0" class="text-warning">
        <mat-icon>warning</mat-icon>
        This tag is currently assigned to {{ data.tag.contactCount }} contact{{ data.tag.contactCount > 1 ? 's' : '' }}.
      </p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-flat-button color="warn" (click)="onConfirm()" [disabled]="isDeleting">
        <mat-spinner diameter="20" *ngIf="isDeleting"></mat-spinner>
        <span *ngIf="!isDeleting">Delete</span>
      </button>
    </mat-dialog-actions>
  `,
  imports: [CommonModule, MaterialModule, MatDialogModule],
})
export class DeleteTagDialogComponent {
  isDeleting = false;

  constructor(
    public dialogRef: MatDialogRef<DeleteTagDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { tag: Tag },
    private tagService: TagService,
    private snackBar: MatSnackBar
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    this.isDeleting = true;
    this.tagService.deleteTag(this.data.tag.id.toString()).subscribe({
      next: () => {
        this.snackBar.open('Tag deleted successfully', 'Close', { duration: 3000 });
        this.dialogRef.close({ event: 'Delete' });
      },
      error: (error) => {
        console.error('Error deleting tag:', error);
        this.snackBar.open('Error deleting tag', 'Close', { duration: 3000 });
        this.isDeleting = false;
      }
    });
  }
}

