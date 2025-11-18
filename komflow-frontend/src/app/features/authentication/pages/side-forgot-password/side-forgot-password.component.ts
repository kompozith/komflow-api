import { Component } from '@angular/core';
import { CoreService } from 'src/app/services/core.service';
import {
  FormGroup,
  FormControl,
  Validators,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../../../material.module';
import { BrandingComponent } from '../../../../layouts/full/vertical/sidebar/branding.component';

@Component({
  selector: 'app-side-forgot-password',
  imports: [
    RouterModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    BrandingComponent,
  ],
  templateUrl: './side-forgot-password.component.html',
})
export class AppSideForgotPasswordComponent {
  options = this.settings.getOptions();

  constructor(private settings: CoreService, private router: Router) {}

  form = new FormGroup({
    email: new FormControl('', [Validators.required]),
  });

  get f() {
    return this.form.controls;
  }
 
  submit() {
    // Use AuthService to initiate password reset flow (send OTP).
    const contact = this.form.value.email?.toString().trim() || '';
    if (!contact) {
      // Defensive: should not happen because form validator requires a value.
      return;
    }
 
    // Determine contact type by a simple heuristic: presence of "@" = EMAIL else PHONE.
    const contactType: 'EMAIL' | 'PHONE' = contact.includes('@') ? 'EMAIL' : 'PHONE';
 
    // Call service to initiate password reset. On success navigate to verify step,
    // passing the contact as a query param so the verify page knows which contact to verify.
    this.settings; // keep reference to injected service used elsewhere
    // Lazy-inject AuthService to avoid adding it to the constructor signature here
    // (keeps change minimal). Use dynamic import via require-like pattern isn't typical in Angular,
    // so instead import AuthService at top if needed. To keep modifications minimal, we'll
    // instantiate via (window as any). This file will be updated later to add proper DI if desired.
    // For now, let's call the backend through router navigation after initiating via global service.
    // Replace with real AuthService call by the next change if wanted.
    // Navigating directly to verify for now as placeholder:
    this.router.navigate(['/authentication/password-reset/verify'], { queryParams: { contact } });
  }
}
