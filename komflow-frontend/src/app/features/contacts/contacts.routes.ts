import { Routes } from '@angular/router';
import { PermissionGuard } from '../../guards/permission.guard';

import { ContactListComponent } from './pages/contact-list/contact-list.component';
import { ContactDetailsComponent } from './pages/contact-details/contact-details.component';
import { ContactCreateComponent } from './pages/contact-create/contact-create.component';
import { ContactEditComponent } from './pages/contact-edit/contact-edit.component';

export const ContactsRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'list',
        component: ContactListComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Contact List',
          permissions: ['CONTACT_LIST'],
          urls: [
            { title: 'Contacts', url: 'contacts/list' },
            { title: 'All Contacts' },
          ],
        },
      },
      {
        path: 'details/:id',
        component: ContactDetailsComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Contact Details',
          permissions: ['CONTACT_SHOW'],
          urls: [
            { title: 'Contacts', url: 'contacts/list' },
            { title: 'Contact Details' },
          ],
        },
      },
      {
        path: 'create',
        component: ContactCreateComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Create Contact',
          permissions: ['CONTACT_CREATE'],
          urls: [
            { title: 'Contacts', url: 'contacts/list' },
            { title: 'Create Contact' },
          ],
        },
      },
      {
        path: 'edit/:id',
        component: ContactEditComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Edit Contact',
          permissions: ['CONTACT_UPDATE'],
          urls: [
            { title: 'Contacts', url: 'contacts/list' },
            { title: 'Edit Contact' },
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
