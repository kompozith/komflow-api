import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type BadgeVariant = 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'outline' | 'info';

@Component({
  selector: 'app-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span
      [ngClass]="getBadgeClasses()"
      [attr.title]="title"
    >
      {{ content }}
      <ng-content></ng-content>
    </span>
  `,
})
export class BadgeComponent {
  @Input() variant: BadgeVariant = 'primary';
  @Input() content?: string;
  @Input() pill = false;
  @Input() title?: string;

  getBadgeClasses(): string[] {
    const classes = [];
    if (this.variant === 'info') {
        classes.push(`bg-light`);
    } else {
        classes.push(`bg-light-${this.variant}`);
    }

    classes.push(`text-${this.variant}`);

    if (this.pill) {
      classes.push('rounded-pill f-w-600 p-6 p-y-4 f-s-12');
    } else {
      classes.push('badge f-w-600 p-6 p-y-4 f-s-12');
    }
    return classes;
  }
}
