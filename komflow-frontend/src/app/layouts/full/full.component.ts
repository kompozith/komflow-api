import { BreakpointObserver, MediaMatcher } from '@angular/cdk/layout';
import { ChangeDetectorRef, Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { Subscription } from 'rxjs';
import { MatSidenav, MatSidenavContent } from '@angular/material/sidenav';
import { CoreService } from 'src/app/services/core.service';
import { AppSettings } from 'src/app/config';
import { filter } from 'rxjs/operators';
import { NavigationEnd, Router } from '@angular/router';
import { navItems } from './vertical/sidebar/sidebar-data';
import { NavService } from '../../services/nav.service';
import { AppNavItemComponent } from './vertical/sidebar/nav-item/nav-item.component';
import { RouterModule } from '@angular/router';
import { MaterialModule } from 'src/app/material.module';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from './vertical/sidebar/sidebar.component';
import { NgScrollbarModule } from 'ngx-scrollbar';
import { TablerIconsModule } from 'angular-tabler-icons';
import { HeaderComponent } from './vertical/header/header.component';
import { AppHorizontalHeaderComponent } from './horizontal/header/header.component';
import { AppHorizontalSidebarComponent } from './horizontal/sidebar/sidebar.component';
import { AppBreadcrumbComponent } from './shared/breadcrumb/breadcrumb.component';
import { LogoutComponent } from 'src/app/features/authentication/pages/logout/logout.component';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from 'src/app/features/authentication/services/auth.service';
import { AuthUser } from 'src/app/features/user-management/models/user';
import { UserProfileService } from 'src/app/services/user-profile.service';

const MOBILE_VIEW = 'screen and (max-width: 768px)';
const TABLET_VIEW = 'screen and (min-width: 769px) and (max-width: 1024px)';
const MONITOR_VIEW = 'screen and (min-width: 1024px)';
const BELOWMONITOR = 'screen and (max-width: 1023px)';

// for mobile app sidebar
interface apps {
  id: number;
  img: string;
  title: string;
  subtitle: string;
  link: string;
}

interface quicklinks {
  id: number;
  title: string;
  link: string;
}

@Component({
    selector: 'app-full',
    imports: [
        RouterModule,
        AppNavItemComponent,
        MaterialModule,
        CommonModule,
        SidebarComponent,
        NgScrollbarModule,
        TablerIconsModule,
        HeaderComponent,
        AppHorizontalHeaderComponent,
        AppHorizontalSidebarComponent,
        AppBreadcrumbComponent,
    ],
    templateUrl: './full.component.html',

    encapsulation: ViewEncapsulation.None
})
export class FullComponent implements OnInit {
   navItems = navItems;

   currentUser: AuthUser | null = null;

   @ViewChild('leftsidenav')
   public sidenav: MatSidenav;
  resView = false;
  @ViewChild('content', { static: true }) content!: MatSidenavContent;
  private layoutChangesSubscription = Subscription.EMPTY;
  private isMobileScreen = false;
  private isContentWidthFixed = true;
  private isCollapsedWidthFixed = false;
  private htmlElement!: HTMLHtmlElement;

  get options() {
    return this.settings.getOptions();
  }

  logout(
    enterAnimationDuration: string,
    exitAnimationDuration: string
  ): void {
    this.dialog.open(LogoutComponent, {
      width: '290px',
      enterAnimationDuration,
      exitAnimationDuration,
    });
  }

  get isOver(): boolean {
    return this.isMobileScreen;
  }

  get isTablet(): boolean {
    return this.resView;
  }

  // for mobile app sidebar
  apps: apps[] = [
    {
      id: 1,
      img: '/assets/images/svgs/icon-dd-chat.svg',
      title: 'Chat Application',
      subtitle: 'Messages & Emails',
      link: '/apps/chat',
    },
    {
      id: 2,
      img: '/assets/images/svgs/icon-dd-cart.svg',
      title: 'Todo App',
      subtitle: 'Completed task',
      link: '/apps/todo',
    },
    {
      id: 3,
      img: '/assets/images/svgs/icon-dd-invoice.svg',
      title: 'Invoice App',
      subtitle: 'Get latest invoice',
      link: '/apps/invoice',
    },
    {
      id: 4,
      img: '/assets/images/svgs/icon-dd-date.svg',
      title: 'Calendar App',
      subtitle: 'Get Dates',
      link: '/apps/calendar',
    },
    {
      id: 5,
      img: '/assets/images/svgs/icon-dd-mobile.svg',
      title: 'Contact Application',
      subtitle: '2 Unsaved Contacts',
      link: '/apps/contacts',
    },
    {
      id: 6,
      img: '/assets/images/svgs/icon-dd-lifebuoy.svg',
      title: 'Tickets App',
      subtitle: 'Create new ticket',
      link: '/apps/tickets',
    },
    {
      id: 7,
      img: '/assets/images/svgs/icon-dd-message-box.svg',
      title: 'Email App',
      subtitle: 'Get new emails',
      link: '/apps/email/inbox',
    },
    {
      id: 8,
      img: '/assets/images/svgs/icon-dd-application.svg',
      title: 'Courses',
      subtitle: 'Create new course',
      link: '/apps/courses',
    },
  ];

  quicklinks: quicklinks[] = [
    {
      id: 1,
      title: 'Pricing Page',
      link: '/theme-pages/pricing',
    },
    {
      id: 2,
      title: 'Authentication Design',
      link: '/authentication/login',
    },
    {
      id: 3,
      title: 'Register Now',
      link: '/authentication/side-register',
    },
    {
      id: 4,
      title: '404 Error Page',
      link: '/authentication/error',
    },
    {
      id: 5,
      title: 'Notes App',
      link: '/apps/notes',
    },
    {
      id: 6,
      title: 'Employee App',
      link: '/apps/employee',
    },
    {
      id: 7,
      title: 'Todo Application',
      link: '/apps/todo',
    },
    {
      id: 8,
      title: 'Treeview',
      link: '/theme-pages/treeview',
    },
  ];

  constructor(
     private settings: CoreService,
     public dialog: MatDialog,
     private mediaMatcher: MediaMatcher,
     private router: Router,
     private breakpointObserver: BreakpointObserver,
     private navService: NavService,
     private cdr: ChangeDetectorRef,
     private authService: AuthService,
     private userProfileService: UserProfileService
   ) {
    this.htmlElement = document.querySelector('html')!;
    this.layoutChangesSubscription = this.breakpointObserver
      .observe([MOBILE_VIEW, TABLET_VIEW, MONITOR_VIEW, BELOWMONITOR])
      .subscribe((state) => {
        // SidenavOpened must be reset true when layout changes
        const updatedOptions = {
          ...this.options,
          sidenavOpened: true,
          sidenavCollapsed: this.options.sidenavCollapsed == false ? state.breakpoints[TABLET_VIEW] : this.options.sidenavCollapsed
        };
        this.settings.setOptions(updatedOptions);
        this.isMobileScreen = state.breakpoints[BELOWMONITOR];
        this.isContentWidthFixed = state.breakpoints[MONITOR_VIEW];
        this.resView = state.breakpoints[BELOWMONITOR];
      });

    // Initialize project theme with options
    this.receiveOptions(this.options);

    // This is for scroll to top
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((e) => {
        this.content.scrollTo({ top: 0 });
      });
  }

  isFilterNavOpen = false;

  toggleFilterNav() {
    this.isFilterNavOpen = !this.isFilterNavOpen;
    console.log('Sidebar open:', this.isFilterNavOpen);
    this.cdr.detectChanges(); // Ensures Angular updates the view
  }

  ngOnInit(): void {
     // Subscribe to current user changes
     this.authService.currentUser$.subscribe(user => {
        this.currentUser = user;
        this.cdr.detectChanges();
     });
  }

  ngOnDestroy() {
    this.layoutChangesSubscription.unsubscribe();
  }

  toggleCollapsed() {
    this.isContentWidthFixed = false;
    const updatedOptions = {
      ...this.options,
      sidenavCollapsed: !this.options.sidenavCollapsed
    };
    this.settings.setOptions(updatedOptions);
  }

  onSidenavClosedStart() {
    this.isContentWidthFixed = false;
  }

  onSidenavOpenedChange(isOpened: boolean) {
    this.isCollapsedWidthFixed = !this.isOver;
    const updatedOptions = {
      ...this.options,
      sidenavOpened: isOpened
    };
    this.settings.setOptions(updatedOptions);
  }

  receiveOptions(options: AppSettings): void {
    this.toggleDarkTheme(options);
    this.toggleColorsTheme(options);
  }

  toggleDarkTheme(options: AppSettings) {
    if (options.theme === 'dark') {
      this.htmlElement.classList.add('dark-theme');
      this.htmlElement.classList.remove('light-theme');
    } else {
      this.htmlElement.classList.remove('dark-theme');
      this.htmlElement.classList.add('light-theme');
    }
  }

  toggleColorsTheme(options: AppSettings) {
     // Remove any existing theme class dynamically
     this.htmlElement.classList.forEach((className) => {
       if (className.endsWith('_theme')) {
         this.htmlElement.classList.remove(className);
       }
     });

     // Add the selected theme class
     this.htmlElement.classList.add(options.activeTheme);
   }

   getUserInitials(): string {
     return this.userProfileService.getUserInitials(this.currentUser);
   }

   getUserProfileImage(): string | null {
     return this.userProfileService.getUserProfileImage(this.currentUser);
   }

   getUserBadgeClass(): string {
     return this.userProfileService.getUserBadgeClass(this.currentUser?.id);
   }
}
