import { Routes } from '@angular/router';
import { RolesListComponent } from './pages/roles-list/roles-list.component';
import { RoleDetailsComponent } from './pages/role-details/role-details.component';

export const RolesRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'list',
        component: RolesListComponent,
        data: {
          title: 'Role List',
          urls: [
            { title: 'Roles Management', url: 'roles/list' },
            { title: 'Roles List' },
          ],
        },
      },
      {
        path: 'details/:id',
        component: RoleDetailsComponent,
        data: {
          title: 'Role Details',
          urls: [
            { title: 'Roles Management', url: 'roles/list' },
            { title: 'Role Details' },
          ],
        },
      },
      {
        path: '',
        redirectTo: 'list',
        pathMatch: 'full',
      },
    ],
  },
];