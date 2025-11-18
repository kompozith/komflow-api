import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RoleService } from './role.service';
import { Role, RoleDetail, RolePage, RoleFilters, CreateRoleRequest, UpdateRoleRequest, Policy } from '../models/role';

@Injectable({
  providedIn: 'root'
})
export class CustomRolesFacade {

  constructor(
    private roleService: RoleService
  ) {}

  // Role operations
  getRoles(filters: RoleFilters = {}): Observable<RolePage> {
    return this.roleService.getRoles(filters);
  }

  getRoleById(id: string): Observable<RoleDetail> {
    return this.roleService.getRoleById(id);
  }

  createRole(role: CreateRoleRequest): Observable<Role> {
    return this.roleService.createRole(role);
  }

  updateRole(id: string, role: UpdateRoleRequest): Observable<Role> {
    return this.roleService.updateRole(id, role);
  }

  deleteRole(id: string): Observable<void> {
    return this.roleService.deleteRole(id);
  }

  getPolicies(resource?: string): Observable<Policy[]> {
    return this.roleService.getPolicies(resource);
  }

  assignPoliciesToRole(roleId: string, policyIds: string[]): Observable<void> {
    return this.roleService.assignPoliciesToRole(roleId, policyIds);
  }

  // Search and filter helpers
  searchRoles(query: string, filters: RoleFilters = {}): Observable<RolePage> {
    return this.getRoles({ ...filters, search: query });
  }

  getActiveRoles(filters: RoleFilters = {}): Observable<RolePage> {
    return this.getRoles({ ...filters, isActive: true });
  }

  getSystemRoles(filters: RoleFilters = {}): Observable<RolePage> {
    return this.getRoles({ ...filters, isSystem: true });
  }

  getCustomRoles(filters: RoleFilters = {}): Observable<RolePage> {
    return this.getRoles({ ...filters, isSystem: false });
  }
}