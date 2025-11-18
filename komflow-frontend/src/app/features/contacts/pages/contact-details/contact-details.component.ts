import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { ContactService } from '../../services/contact.service';
import { Contact } from '../../models/contact';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-contact-details',
  templateUrl: './contact-details.component.html',
  styleUrls: ['./contact-details.component.scss'],
  imports: [
    MaterialModule,
    TablerIconsModule,
    CommonModule,
    BadgeComponent,
  ],
})
export class ContactDetailsComponent implements OnInit {
  contactId: string = '';
  contact: Contact | null = null;
  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private contactService: ContactService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.contactId = this.route.snapshot.params['id'];
    this.loadContact();
  }

  loadContact(): void {
    this.isLoading = true;
    this.contactService.getContactById(this.contactId).subscribe({
      next: (contact) => {
        this.contact = contact;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading contact:', error);
        this.snackBar.open('Error loading contact details', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  editContact(): void {
    this.router.navigate(['contacts/edit', this.contactId]);
  }

  goBack(): void {
    this.router.navigate(['/contacts/list']);
  }

  getContactInitials(firstName: string, lastName: string): string {
    return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
  }

  getContactBadgeClass(contactId: string): string {
    const badgeClasses = [
      'bg-light-primary text-primary',
      'bg-light-success text-success',
      'bg-light-warning text-warning',
      'bg-light-error text-error',
      'bg-light text-info'
    ];

    let hash = 5381;
    for (let i = 0; i < contactId.length; i++) {
      const char = contactId.charCodeAt(i);
      hash = ((hash << 5) + hash) + char;
    }

    const index = Math.abs(hash) % badgeClasses.length;
    return badgeClasses[index];
  }
}
