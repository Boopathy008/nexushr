export type Role = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role?: Role;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  username: string;
  email: string;
  role: Role;
  userId: string;
}

export interface AuthUser {
  username: string;
  email: string;
  role: Role;
  userId: string;
}