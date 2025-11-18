import { Component, signal, inject, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, AbstractControl, ValidationErrors, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from 'src/app/features/authentication/services/auth.service';
import { CoreService } from 'src/app/services/core.service';
import { MaterialModule } from '../../../../material.module';
import { BrandingComponent } from '../../../../layouts/full/vertical/sidebar/branding.component';
import { PasswordVisibilityToggleComponent } from '../../../../components/shared/password-visibility-toggle.component';
import { PasswordResetCompleteResponse } from '../../models/password-reset-complete-response';

@Component({
  selector: 'app-password-reset-complete',
  imports: [RouterModule, MaterialModule, MatInputModule, FormsModule, ReactiveFormsModule, BrandingComponent, PasswordVisibilityToggleComponent],
  templateUrl: './password-reset-complete.component.html',
  styleUrls: ['../login/login.component.scss']
})
export class PasswordResetCompleteComponent implements OnInit {
  private authService = inject(AuthService);
  options = inject(CoreService).getOptions();
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form = new FormGroup({
    newPassword: new FormControl('', { validators: [Validators.required, Validators.minLength(8)], updateOn: 'blur' }),
    confirmPassword: new FormControl('', { validators: [Validators.required], updateOn: 'blur' })
  }, { validators: this.passwordsMatchValidator.bind(this) });

  isLoading = signal(false);
  error = signal<string | null>(null);
  resetToken: string | null = null;
  passwordVisible = signal(false);
  confirmPasswordVisible = signal(false);
  
  // Track fields being edited to hide errors during typing
  private fieldsBeingEdited = new Set<string>();

  ngOnInit(): void {
    this.resetToken = this.route.snapshot.queryParams['resetToken'] || null;
    
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

  passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const group = control as FormGroup;
    const np = group.get('newPassword')?.value;
    const cp = group.get('confirmPassword')?.value;
    return np && cp && np === cp ? null : { passwordMismatch: true };
  }

  togglePasswordVisibility(): void {
    this.passwordVisible.update(visible => !visible);
  }

  toggleConfirmPasswordVisibility(): void {
    this.confirmPasswordVisible.update(visible => !visible);
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
    
    // Also update form-level validation for password match
    this.form.updateValueAndValidity();
  }
  
  isFieldBeingEdited(fieldName: string): boolean {
    return this.fieldsBeingEdited.has(fieldName);
  }

  submit(): void {
    this.error.set(null);
    if (this.form.invalid || !this.resetToken) {
      Object.values(this.form.controls).forEach(c => c.markAsTouched());
      if (!this.resetToken) { this.error.set('Reset token missing'); }
      return;
    }
    this.isLoading.set(true);
    const newPassword = this.form.value.newPassword as string;
    this.authService.completePasswordReset(this.resetToken, newPassword).subscribe({
      next: (res: PasswordResetCompleteResponse) => {
        this.router.navigate(['/authentication/login'], { queryParams: { reset: 'success' } });
      },
      error: (err) => {
        this.error.set(err?.message || 'Failed to reset password');
        this.isLoading.set(false);
      },
      complete: () => {
        this.isLoading.set(false);
      }
    });
  }
}