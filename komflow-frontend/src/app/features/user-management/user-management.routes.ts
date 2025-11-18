import { Routes } from '@angular/router';

import { UserListComponent } from './pages/user-list/user-list.component';
import { DriverListComponent } from './pages/driver-list/driver-list.component';
import { CustomerListComponent } from './pages/customer-list/customer-list.component';
import { VendorListComponent } from './pages/vendor-list/vendor-list.component';

export const UserManagementRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'all-users',
        component: UserListComponent,
        data: {
          title: 'User List',
          urls: [
            { title: 'User Management', url: 'user-management/all-users' },
            { title: 'All Users' },
          ],
        },
      },
      {
        path: 'customers',
        component: CustomerListComponent,
        data: {
          title: 'Customer List',
          urls: [
            { title: 'User Management', url: 'user-management/all-users' },
            { title: 'Customers' },
          ],
        },
      },
      {
        path: 'vendors',
        component: VendorListComponent,
        data: {
          title: 'Vendors List',
          urls: [
            { title: 'User Management', url: 'user-management/all-users' },
            { title: 'Vendors' },
          ],
        },
      },
      {
        path: 'drivers',
        component: DriverListComponent,
        data: {
          title: 'Driver List',
          urls: [
            { title: 'User Management', url: 'user-management/all-users' },
            { title: 'Drivers' },
          ],
        },
      },
      {
        path: '',
        redirectTo: 'all-users',
        pathMatch: 'full',
      },
    ],
  },
];