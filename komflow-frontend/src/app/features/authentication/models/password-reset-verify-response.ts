export interface PasswordResetVerifyData {
  resetToken: string;
  message: string;
  expiresInMinutes: number;
}

export interface PasswordResetVerifyResponse {
  status: string;
  message: string;
  data: PasswordResetVerifyData;
  code: string;
  timestamp: string;
  path: string;
}