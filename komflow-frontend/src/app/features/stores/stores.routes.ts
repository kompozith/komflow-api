import { Routes } from '@angular/router';

import { StoreListComponent } from './pages/store-list/store-list.component';
import { StoreDetailsComponent } from './pages/store-details/store-details.component';
import { StoreCreateComponent } from './pages/store-create/store-create.component';
import { StoreEditComponent } from './pages/store-edit/store-edit.component';

export const StoresRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'list',
        component: StoreListComponent,
        data: {
          title: 'Store List',
          urls: [
            { title: 'Stores', url: 'stores/list' },
            { title: 'All Stores' },
          ],
        },
      },
      {
        path: 'details/:id',
        component: StoreDetailsComponent,
        data: {
          title: 'Store Details',
          urls: [
            { title: 'Stores', url: 'stores/list' },
            { title: 'Store Details' },
          ],
        },
      },
      {
        path: 'create',
        component: StoreCreateComponent,
        data: {
          title: 'Create Store',
          urls: [
            { title: 'Stores', url: 'stores/list' },
            { title: 'Create Store' },
          ],
        },
      },
      {
        path: 'edit/:id',
        component: StoreEditComponent,
        data: {
          title: 'Edit Store',
          urls: [
            { title: 'Stores', url: 'stores/list' },
            { title: 'Edit Store' },
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