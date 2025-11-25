import { AuthUser } from "./auth-user";

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    createdAt: string;
    phoneNumbers?: any[];
  };
  permissions: {
    permissions: string[];
    roles: string[];
  };
}
