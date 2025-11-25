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
import { ContactService } from '../../services/contact.service';
import { CreateContactRequest } from '../../models/contact';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-contact-create',
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
  templateUrl: './contact-create.component.html',
  styleUrl: './contact-create.component.scss',
})
export class ContactCreateComponent {
  contactForm: FormGroup;
  isLoading = false;
  availableTags: any[] = []; // TODO: Load from service

  constructor(
    public dialogRef: MatDialogRef<ContactCreateComponent>,
    private fb: FormBuilder,
    private contactService: ContactService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.contactForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      tagIds: [[]]
    });
  }

  doAction(): void {
    if (this.contactForm.valid) {
      this.isLoading = true;
      const formValue = this.contactForm.value;

      const contactData: CreateContactRequest = {
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        email: formValue.email,
        phone: formValue.phone || undefined,
        tagIds: formValue.tagIds || []
      };

      this.contactService.createContact(contactData).subscribe({
        next: (contact) => {
          this.snackBar.open('Contact created successfully', 'Close', { duration: 3000 });
          this.dialogRef.close({ event: 'Create' });
        },
        error: (error) => {
          console.error('Error creating contact:', error);
          this.snackBar.open('Error creating contact', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  closeDialog(): void {
    this.dialogRef.close({ event: 'Cancel' });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.contactForm.controls).forEach(key => {
      const control = this.contactForm.get(key);
      control?.markAsTouched();
    });
  }
}
