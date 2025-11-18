import { Routes } from '@angular/router';
import { BlankComponent } from './layouts/blank/blank.component';
import { FullComponent } from './layouts/full/full.component';
import { AuthGuard } from './features/authentication/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: FullComponent,
    children: [
      {
        path: 'user-management',
        loadChildren: () =>
          import('./features/user-management/user-management.routes').then(
            (m) => m.UserManagementRoutes
          ),
      },
      {
        path: 'roles',
        loadChildren: () =>
          import('./features/custom-roles/roles.routes').then(
            (m) => m.RolesRoutes
          ),
      },
      {
        path: 'brands',
        loadChildren: () =>
          import('./features/brands/brands.routes').then(
            (m) => m.BrandsRoutes
          ),
      },
      {
        path: 'stores',
        loadChildren: () =>
          import('./features/stores/stores.routes').then(
            (m) => m.StoresRoutes
          ),
      },
      {
        path: 'contacts',
        loadChildren: () =>
          import('./features/contacts/contacts.routes').then(
            (m) => m.ContactsRoutes
          ),
      },
      {
        path: 'tags',
        loadChildren: () =>
          import('./features/tags/tags.routes').then(
            (m) => m.TagsRoutes
          ),
      },
      {
        path: 'messages',
        loadChildren: () =>
          import('./features/messages/messages.routes').then(
            (m) => m.MessagesRoutes
          ),
      },
      {
        path: 'campaigns',
        loadChildren: () =>
          import('./features/campaigns/campaigns.routes').then(
            (m) => m.CampaignsRoutes
          ),
      },
      {
        path: 'files',
        loadChildren: () =>
          import('./features/files/files.routes').then(
            (m) => m.FilesRoutes
          ),
      },
      {
        path: 'audit',
        loadChildren: () =>
          import('./features/audit/audit.routes').then(
            (m) => m.AuditRoutes
          ),
      },
      {
        path: '',
        redirectTo: '/dashboards/dashboard1',
        pathMatch: 'full',
      },
      {
        path: 'starter',
        loadChildren: () =>
          import('./features/features.routes').then((m) => m.PagesRoutes),
      },
      {
        path: 'dashboards',
        loadChildren: () =>
          import('./features/dashboards/dashboards.routes').then(
            (m) => m.DashboardsRoutes
          ),
      },

      {
        path: 'forms',
        loadChildren: () =>
          import('./features/forms/forms.routes').then((m) => m.FormsRoutes),
      },
      {
        path: 'charts',
        loadChildren: () =>
          import('./features/charts/charts.routes').then((m) => m.ChartsRoutes),
      },
      {
        path: 'apps',
        loadChildren: () =>
          import('./features/apps/apps.routes').then((m) => m.AppsRoutes),
      },
      {
        path: 'widgets',
        loadChildren: () =>
          import('./features/widgets/widgets.routes').then((m) => m.WidgetsRoutes),
      },
      {
        path: 'tables',
        loadChildren: () =>
          import('./features/tables/tables.routes').then((m) => m.TablesRoutes),
      },
      {
        path: 'datatable',
        loadChildren: () =>
          import('./features/datatable/datatable.routes').then(
            (m) => m.DatatablesRoutes
          ),
      },
      {
        path: 'theme-pages',
        loadChildren: () =>
          import('./features/theme-pages/theme-pages.routes').then(
            (m) => m.ThemePagesRoutes
          ),
      },
      {
        path: 'ui-components',
        loadChildren: () =>
          import('./features/ui-components/ui-components.routes').then(
            (m) => m.UiComponentsRoutes
          ),
      },
    ],
  },
  {
    path: '',
    component: BlankComponent,
    children: [
      {
        path: 'authentication',
        loadChildren: () =>
          import('./features/authentication/authentication.routes').then(
            (m) => m.AuthenticationRoutes
          ),
      },
      {
        path: 'landingpage',
        loadChildren: () =>
          import('./features/theme-pages/landingpage/landingpage.routes').then(
            (m) => m.LandingPageRoutes
          ),
      },
      {
        path: 'front-pages',
        loadChildren: () =>
          import('./features/front-pages/front-pages.routes').then(
            (m) => m.FrontPagesRoutes
          ),
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'authentication/error',
  },
];
