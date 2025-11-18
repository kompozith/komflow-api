import { Routes } from '@angular/router';
import { PermissionGuard } from '../../guards/permission.guard';

import { FilesListComponent } from './pages/files-list/files-list.component';

export const FilesRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'list',
        component: FilesListComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Files',
          permissions: ['FILE_LIST'],
          urls: [
            { title: 'Files', url: 'files/list' },
            { title: 'All Files' },
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
