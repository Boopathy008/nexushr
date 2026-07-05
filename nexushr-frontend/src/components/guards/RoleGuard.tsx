import { Navigate } from 'react-router-dom';
import type { Role } from '../../types/auth';
import { useAuth } from '../../hooks/useAuth';

interface Props {
  allowedRoles: Role[];
  children: React.ReactNode;
}

export function RoleGuard({ allowedRoles, children }: Props) {
  const { hasAnyRole } = useAuth();
  return hasAnyRole(...allowedRoles) ? <>{children}</> : <Navigate to="/dashboard" replace />;
}