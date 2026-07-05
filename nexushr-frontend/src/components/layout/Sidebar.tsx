import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  LayoutDashboard, Users, Clock, Calendar, DollarSign, Star, Brain, LogOut,
} from 'lucide-react';

const nav = [
  { to: '/dashboard',   label: 'Dashboard',   icon: LayoutDashboard, roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { to: '/employees',   label: 'Employees',   icon: Users,           roles: ['ADMIN','MANAGER'] },
  { to: '/attendance',  label: 'Attendance',  icon: Clock,           roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { to: '/leaves',      label: 'Leave',       icon: Calendar,        roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { to: '/payroll',         label: 'My Payslip',  icon: DollarSign, roles: ['MANAGER','EMPLOYEE'] },
  { to: '/payroll/reports', label: 'Payroll',     icon: DollarSign, roles: ['ADMIN'] },
  { to: '/performance', label: 'Performance', icon: Star,            roles: ['ADMIN','MANAGER','EMPLOYEE'] },
  { to: '/intelligence',label: 'AI Intel',    icon: Brain,           roles: ['ADMIN','MANAGER'] },
] as const;

export function Sidebar({ open, onClose }: { open: boolean; onClose?: () => void }) {
  const { user, logout, hasAnyRole } = useAuth();

  return (
    <aside
      className={`
        fixed lg:static inset-y-0 left-0 z-40
        ${open ? 'w-60 translate-x-0' : 'w-0 -translate-x-full lg:translate-x-0 overflow-hidden'}
        transition-all duration-300 bg-white border-r border-gray-200 flex flex-col shrink-0
      `}
    >
      <div className="px-6 py-5 border-b border-gray-100">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center shrink-0">
            <span className="text-white font-bold text-sm">N</span>
          </div>
          <span className="font-bold text-gray-900 whitespace-nowrap">NexusHR</span>
        </div>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {nav
          .filter((n) => hasAnyRole(...(n.roles as any)))
          .map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition ${
                  isActive ? 'bg-blue-50 text-blue-700' : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`
              }
            >
              <Icon className="w-4 h-4 shrink-0" />
              {label}
            </NavLink>
          ))}
      </nav>

      <div className="px-3 py-4 border-t border-gray-100">
        <div className="px-3 py-2 mb-1">
          <p className="text-sm font-medium text-gray-900">{user?.username}</p>
          <p className="text-xs text-gray-400">{user?.role}</p>
        </div>
        <button
          onClick={logout}
          className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-gray-600 hover:bg-red-50 hover:text-red-600 transition"
        >
          <LogOut className="w-4 h-4 shrink-0" />
          Sign out
        </button>
      </div>
    </aside>
  );
}