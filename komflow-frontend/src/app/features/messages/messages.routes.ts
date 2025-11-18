import { Routes } from '@angular/router';
import { PermissionGuard } from '../../guards/permission.guard';

import { MessageListComponent } from './pages/message-list/message-list.component';
import { MessageCreateComponent } from './pages/message-create/message-create.component';

export const MessagesRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'list',
        component: MessageListComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Message List',
          permissions: ['MESSAGE_LIST'],
          urls: [
            { title: 'Messages', url: 'messages/list' },
            { title: 'All Messages' },
          ],
        },
      },
      {
        path: 'create',
        component: MessageCreateComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Create Message',
          permissions: ['MESSAGE_CREATE'],
          urls: [
            { title: 'Messages', url: 'messages/list' },
            { title: 'Create Message' },
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
