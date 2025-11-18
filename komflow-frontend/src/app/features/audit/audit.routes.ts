import { Routes } from '@angular/router';
import { PermissionGuard } from '../../guards/permission.guard';

import { AuditListComponent } from './pages/audit-list/audit-list.component';

export const AuditRoutes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'list',
        component: AuditListComponent,
        canActivate: [PermissionGuard],
        data: {
          title: 'Audit Log',
          permissions: ['AUDIT_VIEW'],
          urls: [
            { title: 'Audit', url: 'audit/list' },
            { title: 'All Logs' },
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
