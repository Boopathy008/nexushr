import { useEffect, useState } from 'react';
import { Users, Clock, Calendar, Star, CheckCircle, Target } from 'lucide-react';
import { dashboardApi } from '../../api/dashboardApi';
import { StatCard } from '../../components/ui/StatCard';
import { Badge } from '../../components/ui/Badge';
import { useAuth } from '../../hooks/useAuth';

export function ManagerDashboard() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const { user } = useAuth();
  const managerId = user?.userId ?? '';

  useEffect(() => {
    if (!managerId) { setLoading(false); return; }
    dashboardApi.getManager(managerId)
      .then((r) => setData(r.data))
      .catch(() => setError('Could not load manager dashboard'))
      .finally(() => setLoading(false));
  }, [managerId]);

  if (loading) return (
    <div className="flex h-64 items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
    </div>
  );

  if (!managerId) {
    return (
      <div className="p-6 bg-amber-50 border border-amber-200 rounded-xl text-amber-800 text-sm">
        Manager profile not linked yet. Please log in with a valid manager account.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Manager Dashboard</h1>
        <p className="text-gray-500 mt-1">{data?.departmentName ?? 'Your'} team overview</p>
      </div>

      {error && <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">{error}</div>}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard
          title="Team Members"
          value={data?.totalTeamMembers ?? 0}
          icon={Users}
          color="bg-blue-500"
          accent="border-blue-500"
          sub="Active members"
        />
        <StatCard
          title="Checked In Today"
          value={data?.checkedInToday ?? 0}
          icon={Clock}
          color="bg-green-500"
          accent="border-green-500"
          sub={`${data?.teamAttendancePercentage ?? 0}% attendance`}
        />
        <StatCard
          title="Avg Rating"
          value={(data?.teamAverageRating ?? 0).toFixed(1)}
          icon={Star}
          color="bg-purple-500"
          accent="border-purple-500"
          sub="Team performance"
        />
        <StatCard
          title="Pending Leaves"
          value={data?.pendingLeaveCount ?? 0}
          icon={Calendar}
          color="bg-amber-500"
          accent="border-amber-500"
          sub="Awaiting approval"
        />
        <StatCard
          title="Goals Completed"
          value={data?.goalsCompletedThisQuarter ?? 0}
          icon={CheckCircle}
          color="bg-teal-500"
          accent="border-teal-500"
          sub="This quarter"
        />
        <StatCard
          title="Goals In Progress"
          value={data?.goalsInProgressThisQuarter ?? 0}
          icon={Target}
          color="bg-indigo-500"
          accent="border-indigo-500"
          sub="Currently active"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-900 mb-4">Pending Leave Requests</h2>
          <div className="space-y-3">
            {data?.pendingLeaveRequests?.length ? data.pendingLeaveRequests.map((lr: any) => (
              <div key={lr.leaveRequestId} className="flex items-center justify-between p-3 border border-gray-100 rounded-lg">
                <div>
                  <p className="text-sm font-medium text-gray-900">{lr.employeeName}</p>
                  <p className="text-xs text-gray-400">{lr.leaveTypeName} · {lr.startDate} → {lr.endDate} ({lr.totalDays}d)</p>
                </div>
                <Badge color="yellow">PENDING</Badge>
              </div>
            )) : <p className="text-sm text-gray-400">No pending leave requests.</p>}
          </div>
        </div>

        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-900 mb-4">Top Performers</h2>
          <div className="space-y-3">
            {data?.topPerformers?.length ? data.topPerformers.map((m: any) => (
              <div key={m.employeeId} className="flex items-center justify-between p-3 border border-gray-100 rounded-lg">
                <div>
                  <p className="text-sm font-medium text-gray-900">{m.employeeName}</p>
                  <p className="text-xs text-gray-400">{m.designation} · {m.attendancePercentage}% attendance</p>
                </div>
                <span className="text-sm font-bold text-purple-600">{m.performanceRating.toFixed(1)} ★</span>
              </div>
            )) : <p className="text-sm text-gray-400">No top performers data yet.</p>}
          </div>
        </div>
      </div>
    </div>
  );
}