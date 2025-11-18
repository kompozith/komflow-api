import { Component } from '@angular/core';
import { CoreService } from 'src/app/services/core.service';

@Component({
  selector: 'app-branding',
  imports: [],
  template: `
    <a href="/" class="logodark">
      <img
        src="./assets/images/logos/komflow-dark-logo.svg"
        class="align-middle m-2"
        alt="Komflow Logo"
      />
    </a>

    <a href="/" class="logolight">
      <img
        src="./assets/images/logos/komflow-light-logo.svg"
        class="align-middle m-2"
        alt="Komflow Logo"
      />
    </a>
  `,
})
export class BrandingComponent {
  options = this.settings.getOptions();
  constructor(private settings: CoreService) {}

  get appName(): string {
    return 'Komflow';
  }
}
