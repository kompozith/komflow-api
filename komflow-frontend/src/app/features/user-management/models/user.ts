// src/app/features/user-management/models/user.ts
import { UserRole, UserStatus } from '../user-management.constants';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone: string;
  role: UserRole;
  accountStatus: UserStatus;
  profilePicture: string;
  registrationDate: string;
  lastUpdated: string;
}

export interface UserPage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: User[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface UserFilters {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  role?: UserRole;
  status?: UserStatus;
  search?: string;
  registrationDateFrom?: string;
  registrationDateTo?: string;
  lastUpdatedFrom?: string;
  lastUpdatedTo?: string;
}

// AuthUser interface (simplified version for auth context)
export interface AuthUser {
   id: string;
   firstName: string;
   lastName: string;
   email?: string;
   phone?: string;
   role: string;
   accountStatus: string;
   profilePicture?: string;
}