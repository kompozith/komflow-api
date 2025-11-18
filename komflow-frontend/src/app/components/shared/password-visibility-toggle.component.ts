import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-password-visibility-toggle',
  imports: [MatButtonModule, MatIconModule],
  template: `
    <button
      type="button"
      mat-icon-button
      (click)="toggleVisibility.emit()"
      [attr.aria-label]="isVisible() ? 'Hide password' : 'Show password'"
      class="password-toggle-btn"
    >
      <mat-icon>{{ isVisible() ? 'visibility_off' : 'visibility' }}</mat-icon>
    </button>
  `,
  styles: [`
    .password-toggle-btn {
      position: absolute;
      right: 8px;
      top: 50%;
      transform: translateY(-50%);
      min-width: 40px;
      width: 40px;
      height: 40px;
    }

    .password-toggle-btn:hover {
      background: rgba(0, 0, 0, 0.04);
    }

    .password-toggle-btn mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
  `]
})
export class PasswordVisibilityToggleComponent {
  /**
   * Whether the password is currently visible
   */
  isVisible = input.required<boolean>();

  /**
   * Emitted when the visibility toggle is clicked
   */
  toggleVisibility = output<void>();
}