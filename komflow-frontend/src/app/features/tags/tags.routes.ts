import { Routes } from '@angular/router';
import { PermissionGuard } from '../../guards/permission.guard';

import { TagListComponent } from './pages/tag-list/tag-list.component';
import { TagDetailsComponent } from './pages/tag-details/tag-details.component';
import { TagCreateComponent } from './pages/tag-create/tag-create.component';
import { TagEditComponent } from './pages/tag-edit/tag-edit.component';

export const TagsRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'list',
        component: TagListComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Tag List',
          permissions: ['TAG_LIST'],
          urls: [
            { title: 'Tags', url: 'tags/list' },
            { title: 'All Tags' },
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
