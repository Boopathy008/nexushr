import { useEffect, useState } from 'react';
import { Clock, Calendar, DollarSign, Star, Target, CheckCircle } from 'lucide-react';
import { dashboardApi } from '../../api/dashboardApi';
import { StatCard } from '../../components/ui/StatCard';
import { useEmployeeProfile } from '../../hooks/useEmployeeProfile';
import type { EmployeeDashboardResponse } from '../../types/dashboard';

export function EmployeeDashboard() {
  const { employeeId, employeeName, loading: profileLoading } = useEmployeeProfile();
  const [data, setData] = useState<EmployeeDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!employeeId) { setLoading(false); return; }
    dashboardApi.getEmployee(employeeId)
      .then((r) => setData(r.data))
      .finally(() => setLoading(false));
  }, [employeeId]);

  if (profileLoading || loading) return (
    <div className="flex h-64 items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
    </div>
  );

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">
          Welcome back{employeeName ? `, ${employeeName.split(' ')[0]}` : ''}
        </h1>
        <p className="text-gray-500 mt-1">{data?.designation} · {data?.department} · {data?.tenureLabel}</p>
      </div>

      {/* 6-card grid matching Admin/Manager layout */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard
          title="Today's Status"
          value={data?.checkedInToday ? 'Checked In' : 'Not Checked In'}
          icon={Clock}
          color="bg-blue-500"
          accent="border-blue-500"
          sub={data?.workingHoursToday ?? '—'}
        />
        <StatCard
          title="Attendance This Month"
          value={`${data?.attendancePercentageThisMonth ?? 0}%`}
          icon={Calendar}
          color="bg-green-500"
          accent="border-green-500"
          sub={`${data?.presentDaysThisMonth ?? 0}/${data?.workingDaysThisMonth ?? 0} days`}
        />
        <StatCard
          title="Performance Rating"
          value={Number(data?.averagePerformanceRating ?? 0).toFixed(1)}
          icon={Star}
          color="bg-purple-500"
          accent="border-purple-500"
          sub={data?.ratingLabel ?? '—'}
        />
        <StatCard
          title="Last Payslip"
          value={`₹${Number(data?.lastMonthNetSalary ?? 0).toLocaleString('en-IN')}`}
          icon={DollarSign}
          color="bg-teal-500"
          accent="border-teal-500"
          sub={data?.lastPayslipPeriod ?? '—'}
        />
        <StatCard
          title="Goals In Progress"
          value={data?.goalsInProgress ?? 0}
          icon={Target}
          color="bg-amber-500"
          accent="border-amber-500"
          sub="Active goals"
        />
        <StatCard
          title="Goals Completed"
          value={data?.goalsCompleted ?? 0}
          icon={CheckCircle}
          color="bg-indigo-500"
          accent="border-indigo-500"
          sub="Finished goals"
        />
      </div>

      {/* Leave Balance panel */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <h2 className="font-semibold text-gray-900 mb-4">Leave Balance</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {data?.leaveBalances?.length ? data.leaveBalances.map((lb) => (
            <div key={lb.leaveTypeCode} className="p-4 bg-gray-50 rounded-xl border border-gray-100">
              <p className="text-xs text-gray-500 mb-1 font-medium uppercase tracking-wide">{lb.leaveTypeName}</p>
              <p className="text-2xl font-bold text-gray-900">{lb.availableDays}</p>
              <p className="text-xs text-gray-400 mt-0.5">of {lb.totalDays} days available</p>
              <div className="mt-2 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                <div
                  className="h-full bg-blue-500 rounded-full"
                  style={{ width: `${Math.min((Number(lb.usedDays) / Number(lb.totalDays)) * 100, 100)}%` }}
                />
              </div>
            </div>
          )) : <p className="text-sm text-gray-400 col-span-3">No leave balance data.</p>}
        </div>
      </div>
    </div>
  );
}