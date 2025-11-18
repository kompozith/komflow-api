import { Injectable } from '@angular/core';
import { Observable, combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserService } from './user.service';
import { User, UserFilters, UserPage } from '../models/user';
import { UserRole, UserStatus } from '../user-management.constants';

@Injectable({
  providedIn: 'root'
})
export class UserManagementFacade {

  constructor(
    private userService: UserService
  ) {}

  // User operations
  getUsers(filters: UserFilters = {}): Observable<UserPage> {
    return this.userService.getUsers(filters);
  }

  getUserById(id: string): Observable<User> {
    return this.userService.getUserById(id);
  }

  createUser(user: Partial<User>): Observable<User> {
    return this.userService.createUser(user);
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    return this.userService.updateUser(id, user);
  }

  deleteUser(id: string): Observable<void> {
    return this.userService.deleteUser(id);
  }

  updateUserStatus(id: string, status: UserStatus): Observable<User> {
    return this.userService.updateUserStatus(id, status);
  }

  // Combined operations for dashboard/stats
  getUserStats(): Observable<{
    totalUsers: number;
    activeUsers: number;
    suspendedUsers: number;
  }> {
    return this.getUsers({ size: 1000 }).pipe(
      map(page => {
        const users = page.content;
        return {
          totalUsers: page.totalElements,
          activeUsers: users.filter((u: User) => u.accountStatus === 'ACTIVE').length,
          suspendedUsers: users.filter((u: User) => u.accountStatus === 'SUSPENDED').length
        };
      })
    );
  }

  // Bulk operations
  bulkUpdateStatus(userIds: string[], status: UserStatus): Observable<User[]> {
    const operations = userIds.map(id => this.updateUserStatus(id, status));
    return combineLatest(operations);
  }

  // Search and filter helpers
  searchUsers(query: string, filters: UserFilters = {}): Observable<UserPage> {
    return this.getUsers({ ...filters, search: query });
  }

  getUsersByRole(role: UserRole): Observable<UserPage> {
    return this.getUsers({ role, size: 100 });
  }

  getUsersByStatus(status: UserStatus): Observable<UserPage> {
    return this.getUsers({ status, size: 100 });
  }
}