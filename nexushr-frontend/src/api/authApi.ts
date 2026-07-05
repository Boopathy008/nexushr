import api from './axios';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
  register: (data: RegisterRequest) => api.post<AuthResponse>('/auth/register', data),
  refreshToken: (refreshToken: string) =>
    api.post<AuthResponse>('/auth/refresh-token', { refreshToken }),
  logout: () => api.post('/auth/logout'),
};