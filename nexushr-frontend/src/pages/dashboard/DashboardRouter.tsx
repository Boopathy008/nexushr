import { useAuth } from '../../hooks/useAuth';
import { AdminDashboard } from './AdminDashboard';
import { ManagerDashboard } from './ManagerDashboard';
import { EmployeeDashboard } from './EmployeeDashboard';

export function DashboardRouter() {
  const { user } = useAuth();

  if (user?.role === 'ADMIN') return <AdminDashboard />;
  if (user?.role === 'MANAGER') return <ManagerDashboard />;
  return <EmployeeDashboard />;
}