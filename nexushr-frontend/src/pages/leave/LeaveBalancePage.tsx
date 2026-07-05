import { useEffect, useState } from 'react';
import { leaveApi } from '../../api/leaveApi';
import { useEmployeeProfile } from '../../hooks/useEmployeeProfile';
import type { LeaveBalanceResponse } from '../../types/leave';

export function LeaveBalancePage() {
  const { employeeId, loading: profileLoading } = useEmployeeProfile();
  const [balance, setBalance] = useState<LeaveBalanceResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!employeeId) { setLoading(false); return; }
    leaveApi.getBalance(employeeId).then((r) => setBalance(r.data)).finally(() => setLoading(false));
  }, [employeeId]);

  if (profileLoading || loading) return (
    <div className="flex h-64 items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
    </div>
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Leave Balance</h1>
        <p className="text-gray-500 mt-1">{balance?.year} · {balance?.employeeName}</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {balance?.balances.map((b) => {
          const pct = b.totalDays > 0 ? (b.usedDays / b.totalDays) * 100 : 0;
          return (
            <div key={b.leaveTypeCode} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center justify-between mb-3">
                <p className="font-semibold text-gray-900">{b.leaveTypeName}</p>
                <span className="text-xs font-medium text-gray-400">{b.leaveTypeCode}</span>
              </div>
              <p className="text-3xl font-bold text-blue-600">{b.availableDays}</p>
              <p className="text-xs text-gray-400 mb-3">days available of {b.totalDays}</p>
              <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full bg-blue-500 rounded-full" style={{ width: `${pct}%` }} />
              </div>
              <div className="flex justify-between mt-2 text-xs text-gray-500">
                <span>Used: {b.usedDays}</span>
                <span>Pending: {b.pendingDays}</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}