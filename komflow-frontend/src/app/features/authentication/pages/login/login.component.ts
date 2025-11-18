import { Component, signal, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CoreService } from 'src/app/services/core.service';
import { AuthService } from 'src/app/features/authentication/services/auth.service';
import { FormGroup, FormControl, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { MaterialModule } from '../../../../material.module';
import { BrandingComponent } from '../../../../layouts/full/vertical/sidebar/branding.component';
import { MatInputModule } from '@angular/material/input';
import { PasswordVisibilityToggleComponent } from '../../../../components/shared/password-visibility-toggle.component';
import { LoginRequest } from '../../models/login-request';

@Component({
    selector: 'app-login',
    imports: [RouterModule, MaterialModule, MatInputModule, FormsModule, ReactiveFormsModule, BrandingComponent, PasswordVisibilityToggleComponent],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class AppLoginComponent implements OnInit {
   private authService = inject(AuthService);
   private settings = inject(CoreService);
   private router = inject(Router);
   private route = inject(ActivatedRoute);
   private cdr = inject(ChangeDetectorRef);

  // UI state management
  isLoading = signal(false);
  loginError = signal<string | null>(null);
  passwordVisible = signal(false);

  // Track fields being edited to hide errors during typing
  private fieldsBeingEdited = new Set<string>();

  options = this.settings.getOptions();

  // Initialize controls with username/email login
  // updateOn: 'change' makes validation errors appear on every input change
  form = new FormGroup({
    email: new FormControl('', { validators: [Validators.required], updateOn: 'change' }),
    password: new FormControl('', { validators: [Validators.required], updateOn: 'change' }),
    rememberMe: new FormControl(false)
  });

  ngOnInit(): void {
     this.form.updateValueAndValidity();
     this.cdr.detectChanges();

     // Clear general error when user starts typing in any field
     this.form.valueChanges.subscribe(() => {
       if (this.loginError()) {
         this.loginError.set(null);
       }
     });
   }

   get f() {
     return this.form.controls;
   }


  submit(): void {
    this.loginError.set(null);

    if (this.form.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isLoading.set(true);

    const formValue = this.form.value;
    const loginData: LoginRequest = {
      emailOrPhone: formValue.email || '',
      password: formValue.password || ''
    };

    const returnUrl: string = this.route.snapshot.queryParams['returnUrl'] || '/';

    this.authService.login(loginData, formValue.rememberMe || false).subscribe({
      next: (response) => {
        this.router.navigateByUrl(returnUrl).then(navigated => {
          if (!navigated) {
            this.router.navigate(['/']);
          }
        }).catch(() => {
          this.router.navigate(['/']);
        });
      },
      error: (error) => {
        this.loginError.set(error.message || 'Login failed. Please try again.');
        this.isLoading.set(false);
      },
      complete: () => {
        this.isLoading.set(false);
      }
    });
  }

  togglePasswordVisibility(): void {
    this.passwordVisible.update(visible => !visible);
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

  private markFormGroupTouched(): void {
    Object.keys(this.form.controls).forEach(key => {
      const control = this.form.get(key);
      control?.markAsTouched();
    });
  }
}
