import React, { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import type { AuthUser, Role } from '../types/auth';
import { authApi } from '../api/authApi';

interface AuthContextValue {
  user: AuthUser | null;
  isLoading: boolean;
  login: (usernameOrEmail: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: Role) => boolean;
  hasAnyRole: (...roles: Role[]) => boolean;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (stored) {
      try { setUser(JSON.parse(stored)); } catch { localStorage.clear(); }
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(async (usernameOrEmail: string, password: string) => {
    const { data } = await authApi.login({ usernameOrEmail, password });
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    const authUser: AuthUser = { username: data.username, email: data.email, role: data.role, userId: data.userId };
    localStorage.setItem('user', JSON.stringify(authUser));
    setUser(authUser);
  }, []);

  const logout = useCallback(async () => {
    try { await authApi.logout(); } catch { /* best effort */ }
    localStorage.clear();
    setUser(null);
  }, []);

  const hasRole = useCallback((r: Role) => user?.role === r, [user]);
  const hasAnyRole = useCallback((...roles: Role[]) => roles.some((r) => user?.role === r), [user]);

  const value = useMemo(
    () => ({ user, isLoading, login, logout, hasRole, hasAnyRole }),
    [user, isLoading, login, logout, hasRole, hasAnyRole]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}