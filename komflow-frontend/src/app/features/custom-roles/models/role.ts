export interface Role {
  id: string;
  name: string;
  displayName: string;
  description: string;
  systemRole?: string;
  isSystem: boolean;
  isActive: boolean;
  userCount: number;
  policyCount: number;
  createdBy: string;
  createdByName: string;
  createdAt: string;
  updatedAt: string;
}

export interface RoleDetail extends Role {
  policies: Policy[];
}

export interface Policy {
  id: string;
  name: string;
  description: string;
  policyRule: string;
  type: 'ALLOW' | 'DENY';
  resource: string;
  action: string;
  priority: number;
  isActive: boolean;
  conditionData: string;
  createdAt: string;
  updatedAt: string;
}

export interface RolePage {
  content: Role[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface CreateRoleRequest {
  name: string;
  displayName: string;
  description: string;
  policyIds?: string[];
}

export interface UpdateRoleRequest {
  displayName?: string;
  description?: string;
  isActive?: boolean;
}

export interface RoleFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  search?: string;
  isActive?: boolean;
  isSystem?: boolean;
}