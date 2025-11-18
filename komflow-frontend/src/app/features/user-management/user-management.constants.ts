// src/app/features/user-management/user-management.constants.ts
export const USER_MANAGEMENT_API = {
  BASE: 'admin/users',
} as const;

export const USER_CONFIG = {
  DEFAULT_PAGE_SIZE: 10,
  MAX_PAGE_SIZE: 100,
} as const;

// User roles
export const USER_ROLES = {
  CUSTOMER: 'CUSTOMER',
  SELLER: 'SELLER',
  DELIVERY: 'DELIVERY',
  ADMIN: 'ADMIN',
  SUPER_ADMIN: 'SUPER_ADMIN',
  POLICY_ADMIN: 'POLICY_ADMIN',
  USER_ADMIN: 'USER_ADMIN',
} as const;

export const USER_STATUSES = {
  ACTIVE: 'ACTIVE',
  SUSPENDED: 'SUSPENDED',
  PENDING: 'PENDING',
} as const;

// Type helpers
export type UserRole = typeof USER_ROLES[keyof typeof USER_ROLES];
export type UserStatus = typeof USER_STATUSES[keyof typeof USER_STATUSES];
export type UserManagementApiEndpoints = typeof USER_MANAGEMENT_API[keyof typeof USER_MANAGEMENT_API];