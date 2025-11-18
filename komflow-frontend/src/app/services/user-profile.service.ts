import { Injectable } from '@angular/core';
import { AuthService } from '../features/authentication/services/auth.service';
import { AuthUser } from '../features/user-management/models/user';

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {

  constructor(private authService: AuthService) {}

  /**
   * Get current authenticated user
   */
  getCurrentUser(): AuthUser | null {
    return this.authService.getCurrentUser();
  }

  /**
   * Get user initials from first and last name
   */
  getUserInitials(user?: AuthUser | null): string {
    if (!user) {
      user = this.getCurrentUser();
    }
    if (!user) return 'U';
    return (user.firstName.charAt(0) + user.lastName.charAt(0)).toUpperCase();
  }

  /**
   * Get user profile image URL or null if not available
   */
  getUserProfileImage(user?: AuthUser | null): string | null {
    if (!user) {
      user = this.getCurrentUser();
    }
    return user?.profilePicture || null;
  }

  /**
   * Get user display name (firstName + lastName)
   */
  getUserDisplayName(user?: AuthUser | null): string {
    if (!user) {
      user = this.getCurrentUser();
    }
    if (!user) return 'User';
    return `${user.firstName} ${user.lastName}`;
  }

  /**
   * Get user email
   */
  getUserEmail(user?: AuthUser | null): string {
    if (!user) {
      user = this.getCurrentUser();
    }
    return user?.email || 'user@example.com';
  }

  /**
   * Get consistent badge class for user avatars
   */
  getUserBadgeClass(userId?: string): string {
    if (!userId) {
      const user = this.getCurrentUser();
      userId = user?.id || 'default';
    }

    // Use badge-like classes with dark text and light backgrounds
    const badgeClasses = [
      'bg-light-primary text-primary',
      'bg-light-secondary text-secondary',
      'bg-light-success text-success',
      'bg-light-warning text-warning',
      'bg-light-error text-error',
      'bg-info-subtle text-info'
    ];

    // Improved djb2 hash function for better distribution
    let hash = 5381;
    for (let i = 0; i < userId.length; i++) {
      const char = userId.charCodeAt(i);
      hash = ((hash << 5) + hash) + char; // hash * 33 + char
    }

    // Ensure positive index with better distribution
    const index = Math.abs(hash) % badgeClasses.length;
    return badgeClasses[index];
  }
}