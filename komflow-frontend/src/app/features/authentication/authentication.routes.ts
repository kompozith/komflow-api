import { Routes } from '@angular/router';
import { GuestGuard } from './guards/guest.guard';

import { AppBoxedForgotPasswordComponent } from './pages/boxed-forgot-password/boxed-forgot-password.component';
import { AppBoxedLoginComponent } from './pages/boxed-login/boxed-login.component';
import { AppBoxedRegisterComponent } from './pages/boxed-register/boxed-register.component';
import { AppBoxedTwoStepsComponent } from './pages/boxed-two-steps/boxed-two-steps.component';
import { AppErrorComponent } from './pages/error/error.component';
import { AppMaintenanceComponent } from './pages/maintenance/maintenance.component';
import { AppSideForgotPasswordComponent } from './pages/side-forgot-password/side-forgot-password.component';
import { AppLoginComponent } from './pages/login/login.component';
import { AppSideRegisterComponent } from './pages/side-register/side-register.component';
import { AppSideTwoStepsComponent } from './pages/side-two-steps/side-two-steps.component';

/* Password reset flow components */
import { PasswordResetInitiateComponent } from './pages/password-reset-initiate/password-reset-initiate.component';
import { PasswordResetVerifyComponent } from './pages/password-reset-verify/password-reset-verify.component';
import { PasswordResetCompleteComponent } from './pages/password-reset-complete/password-reset-complete.component';
import {AuthGuard} from "./guards/auth.guard";

export const AuthenticationRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'boxed-forgot-pwd',
        component: AppBoxedForgotPasswordComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'boxed-login',
        component: AppBoxedLoginComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'boxed-register',
        component: AppBoxedRegisterComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'boxed-two-steps',
        component: AppBoxedTwoStepsComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'error',
        component: AppErrorComponent,
      },
      {
        path: 'forbidden',
        component: AppErrorComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'maintenance',
        component: AppMaintenanceComponent,
      },
      {
        path: 'side-forgot-pwd',
        component: AppSideForgotPasswordComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'login',
        component: AppLoginComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'side-register',
        component: AppSideRegisterComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'side-two-steps',
        component: AppSideTwoStepsComponent,
        canActivate: [GuestGuard],
      },
      // Password reset flow
      {
        path: 'password-reset/initiate',
        component: PasswordResetInitiateComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'password-reset/verify',
        component: PasswordResetVerifyComponent,
        canActivate: [GuestGuard],
      },
      {
        path: 'password-reset/complete',
        component: PasswordResetCompleteComponent,
        canActivate: [GuestGuard],
      },
    ],
  },
];
