import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BaseUserListComponent, UserListConfig } from '../base-user-list.component';
import { UserService } from '../../services/user.service';
import { UserManagementFacade } from '../../services/user-management.facade';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-driver-list',
  templateUrl: './driver-list.component.html',
  imports: [
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    TablerIconsModule,
    CommonModule,
    MatMenuModule,
    MatIconModule,
    BadgeComponent,
  ],
})
export class DriverListComponent extends BaseUserListComponent {

  constructor(
    dialog: MatDialog,
    userService: UserService,
    userManagementFacade: UserManagementFacade,
    snackBar: MatSnackBar
  ) {
    super(dialog, userService, userManagementFacade, snackBar);
    this.config = {
      userRole: 'DELIVERY',
      searchPlaceholder: 'Search Driver',
      showRoleColumn: false,
      showRoleChangeAction: false,
      title: 'Driver Management'
    };
  }
}