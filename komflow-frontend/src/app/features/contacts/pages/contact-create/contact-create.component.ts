import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { ContactService } from '../../services/contact.service';
import { CreateContactRequest } from '../../models/contact';

@Component({
  selector: 'app-contact-create',
  templateUrl: './contact-create.component.html',
  styleUrls: [],
  imports: [
    MaterialModule,
    ReactiveFormsModule,
    FormsModule,
    TablerIconsModule,
    CommonModule,
  ],
})
export class ContactCreateComponent implements OnInit {
  contactForm: FormGroup;
  isLoading = false;
  availableTags: any[] = []; // TODO: Load from service

  constructor(
    private fb: FormBuilder,
    private contactService: ContactService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.contactForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      tagIds: [[]]
    });
  }

  ngOnInit(): void {
    // TODO: Load available tags
    // this.loadTags();
  }

  onSubmit(): void {
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
          this.router.navigate(['/contacts/list']);
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

  onCancel(): void {
    this.router.navigate(['/contacts/list']);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.contactForm.controls).forEach(key => {
      const control = this.contactForm.get(key);
      control?.markAsTouched();
    });
  }

  // TODO: Implement tag loading
  // private loadTags(): void {
  //   this.tagService.getTags().subscribe(tags => {
  //     this.availableTags = tags;
  //   });
  // }
}
