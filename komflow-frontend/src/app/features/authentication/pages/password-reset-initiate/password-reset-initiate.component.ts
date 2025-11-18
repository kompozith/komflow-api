import { Component, signal, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CoreService } from 'src/app/services/core.service';
import { FormGroup, FormControl, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../../../material.module';
import { BrandingComponent } from '../../../../layouts/full/vertical/sidebar/branding.component';
import { AuthService } from 'src/app/features/authentication/services/auth.service';
import { NgxIntlTelInputModule, CountryISO, SearchCountryField } from 'ngx-intl-tel-input';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-password-reset-initiate',
  imports: [RouterModule, MaterialModule, MatInputModule, FormsModule, ReactiveFormsModule, NgxIntlTelInputModule, BrandingComponent],
  templateUrl: './password-reset-initiate.component.html',
  styleUrls: ['../login/login.component.scss']
})
export class PasswordResetInitiateComponent implements OnInit {
  private authService = inject(AuthService);
  private settings = inject(CoreService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  options = this.settings.getOptions();
  CountryISO = CountryISO;
  SearchCountryField = SearchCountryField;

  // Mirror login: email/phone tab switch using signals
  activeMode = signal<'email' | 'phone'>('email');

  form = new FormGroup({
    email: new FormControl('', { validators: [], updateOn: 'change' }),
    phoneNumber: new FormControl(null, { validators: [], updateOn: 'change' }),
  });

  isLoading = signal(false);
  error = signal<string | null>(null);
  
  // Track fields being edited to hide errors during typing
  private fieldsBeingEdited = new Set<string>();

  ngOnInit(): void {
    // Ensure validators match active tab on init
    this.updateValidators(this.activeMode());
    this.form.updateValueAndValidity();
    this.cdr.detectChanges();
    
    // Clear general error when user starts typing in any field
    this.form.valueChanges.subscribe(() => {
      if (this.error()) {
        this.error.set(null);
      }
    });
  }

  get f() {
    return this.form.controls;
  }

  setMode(mode: 'email' | 'phone') {
    this.activeMode.set(mode);
    this.updateValidators(mode);
  }

  private updateValidators(mode: 'email' | 'phone') {
    const emailControl = this.form.get('email');
    const phoneControl = this.form.get('phoneNumber');

    if (mode === 'email') {
      emailControl?.setValidators([Validators.required, Validators.email]);
      phoneControl?.clearValidators();
      phoneControl?.setValue(null);
    } else {
      phoneControl?.setValidators([Validators.required]);
      emailControl?.clearValidators();
      emailControl?.setValue('');
    }

    emailControl?.updateValueAndValidity();
    phoneControl?.updateValueAndValidity();

    try {
      this.form.updateValueAndValidity();
      this.cdr.detectChanges();
    } catch { /* ignore */ }
  }

  private formatPhoneNumber(phoneObject: any): string {
    if (!phoneObject) return '';

    if (typeof phoneObject === 'string') return phoneObject;
    if (phoneObject.internationalNumber) return phoneObject.internationalNumber.replace(/\s/g, '').replace(/^\+/, '');
    if (phoneObject.number && phoneObject.dialCode) return `${phoneObject.dialCode}${phoneObject.number}`;
    return '';
  }

  onFieldInput(fieldName: string): void {
    // Mark field as being edited to hide errors during typing
    this.fieldsBeingEdited.add(fieldName);
  }
  
  onFieldBlur(fieldName: string): void {
    // Remove field from being edited set when it loses focus
    this.fieldsBeingEdited.delete(fieldName);
    
    // Trigger validation on blur
    const control = this.form.get(fieldName);
    if (control) {
      control.updateValueAndValidity();
      control.markAsTouched();
    }
  }
  
  isFieldBeingEdited(fieldName: string): boolean {
    return this.fieldsBeingEdited.has(fieldName);
  }

  submit(): void {
    this.error.set(null);

    if (this.form.invalid) {
      Object.keys(this.form.controls).forEach(key => {
        const control = this.form.get(key);
        control?.markAsTouched();
      });
      return;
    }

    this.isLoading.set(true);

    const contact = this.activeMode() === 'email'
      ? (this.form.value.email as string || '')
      : this.formatPhoneNumber(this.form.value.phoneNumber);

    const contactType: 'EMAIL' | 'PHONE' = this.activeMode() === 'email' ? 'EMAIL' : 'PHONE';

    this.authService.initiatePasswordReset(contact, contactType).subscribe({
      next: () => {
        this.router.navigate(['/authentication/password-reset/verify'], { queryParams: { contact } });
      },
      error: (err) => {
        this.error.set(err?.message || 'Unable to initiate password reset');
        this.isLoading.set(false);
      },
      complete: () => {
        this.isLoading.set(false);
      }
    });
  }
}