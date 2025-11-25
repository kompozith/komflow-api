// src/app/features/auth/auth.constants.ts
export const AUTH_API = {
  BASE: 'auth',
  LOGIN: 'login',
  REFRESH: 'refresh',
  LOGOUT: 'logout',
  ME: 'me',
} as const;

export const AUTH_CONFIG = {
    TOKEN_KEY: 'auth_token',
    TOKEN_KEY_EXPIRES: 'auth_token_expires',
    REFRESH_TOKEN_KEY: 'auth_refresh_token',
    USER_KEY: 'auth_user',
    PERMISSIONS_KEY: 'auth_permissions',
    ROLES_KEY: 'auth_roles',
    REMEMBER_KEY: 'auth_remember',
} as const;

// Type helper (utile pour tests/guards)
export type AuthApiEndpoints = typeof AUTH_API[keyof typeof AUTH_API];
