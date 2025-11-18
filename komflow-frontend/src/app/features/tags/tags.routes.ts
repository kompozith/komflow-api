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
        path: 'details/:id',
        component: TagDetailsComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Tag Details',
          permissions: ['TAG_SHOW'],
          urls: [
            { title: 'Tags', url: 'tags/list' },
            { title: 'Tag Details' },
          ],
        },
      },
      {
        path: 'create',
        component: TagCreateComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Create Tag',
          permissions: ['TAG_CREATE'],
          urls: [
            { title: 'Tags', url: 'tags/list' },
            { title: 'Create Tag' },
          ],
        },
      },
      {
        path: 'edit/:id',
        component: TagEditComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Edit Tag',
          permissions: ['TAG_UPDATE'],
          urls: [
            { title: 'Tags', url: 'tags/list' },
            { title: 'Edit Tag' },
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
