import { Routes } from '@angular/router';

import { BrandListComponent } from './pages/brand-list/brand-list.component';

export const BrandsRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'list',
        component: BrandListComponent,
        data: {
          title: 'Brand List',
          urls: [
            { title: 'Brands', url: 'brands/list' },
            { title: 'All Brands' },
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