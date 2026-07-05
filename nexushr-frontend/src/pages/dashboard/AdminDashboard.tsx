import { useEffect, useState } from 'react';
import { Users, UserCheck, Clock, DollarSign, Calendar, TrendingUp } from 'lucide-react';
import { dashboardApi } from '../../api/dashboardApi';
import { StatCard } from '../../components/ui/StatCard';
import type { AdminDashboardResponse } from '../../types/dashboard';

export function AdminDashboard() {
  const [stats, setStats] = useState<AdminDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    dashboardApi.getAdmin()
      .then((r) => setStats(r.data))
      .catch(() => setError('Could not load dashboard'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="flex h-64 items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
    </div>
  );

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-500 mt-1">Welcome back. Here's your workforce overview.</p>
      </div>

      {error && <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">{error}</div>}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard title="Total Employees" value={stats?.totalEmployees ?? 0} icon={Users} color="bg-blue-500" accent="border-blue-500" sub="All records" />
        <StatCard title="Active Employees" value={stats?.activeEmployees ?? 0} icon={UserCheck} color="bg-green-500" accent="border-green-500" sub="Currently active" />
        <StatCard title="Departments" value={stats?.totalDepartments ?? 0} icon={TrendingUp} color="bg-purple-500" accent="border-purple-500" sub="Active departments" />
        <StatCard title="Pending Leaves" value={stats?.pendingLeaveRequests ?? 0} icon={Calendar} color="bg-amber-500" accent="border-amber-500" sub="Awaiting approval" />
        <StatCard title="Payroll This Month" value={`₹${(stats?.totalPayrollThisMonth ?? 0).toLocaleString('en-IN')}`} icon={DollarSign} color="bg-teal-500" accent="border-teal-500" sub="Processed payslips" />
        <StatCard title="Checked In Today" value={stats?.checkedInToday ?? 0} icon={Clock} color="bg-indigo-500" accent="border-indigo-500" sub={`${stats?.todayAttendancePercentage ?? 0}% attendance`} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-900 mb-4">Quick Actions</h2>
          <div className="grid grid-cols-2 gap-3">
            {[
              { label: 'Add Employee', href: '/employees/new' },
              { label: 'Run Payroll', href: '/payroll' },
              { label: 'View Leave Requests', href: '/leaves' },
              { label: 'AI Intelligence', href: '/intelligence' },
            ].map((a) => (
              <a key={a.label} href={a.href}
                className="flex items-center justify-center p-3 border border-gray-200 rounded-lg text-sm font-medium text-gray-700 hover:bg-blue-50 hover:border-blue-300 hover:text-blue-700 transition">
                {a.label}
              </a>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-900 mb-4">Department Breakdown</h2>
          <div className="space-y-3">
            {stats?.departmentHeadcounts?.length ? stats.departmentHeadcounts.map((d) => (
              <div key={d.departmentCode} className="flex items-center justify-between text-sm">
                <span className="text-gray-700">{d.departmentName}</span>
                <span className="font-semibold text-gray-900">{d.activeCount} employees</span>
              </div>
            )) : <p className="text-sm text-gray-400">No department data available.</p>}
          </div>
        </div>
      </div>
    </div>
  );
}