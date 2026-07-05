import { useEffect, useState } from 'react';
import { attendanceApi } from '../../api/attendanceApi';
import { useEmployeeProfile } from '../../hooks/useEmployeeProfile';
import { Badge } from '../../components/ui/Badge';
import { Table } from '../../components/ui/Table';
import type { MonthlyAttendanceReport, AttendanceStatus } from '../../types/attendance';

const statusColor: Record<AttendanceStatus, 'green' | 'red' | 'yellow' | 'gray' | 'blue'> = {
  PRESENT: 'green', ABSENT: 'red', HALF_DAY: 'yellow',
  HOLIDAY: 'gray', WEEKEND: 'gray', ON_LEAVE: 'blue',
};

export function MonthlyReportPage() {
  const { employeeId, loading: profileLoading } = useEmployeeProfile();
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [report, setReport] = useState<MonthlyAttendanceReport | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!employeeId) { setLoading(false); return; }
    setLoading(true);
    attendanceApi.getMonthlyReport(employeeId, year, month)
      .then((r) => setReport(r.data))
      .finally(() => setLoading(false));
  }, [employeeId, year, month]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Monthly Attendance Report</h1>
        <div className="flex gap-2">
          <select value={month} onChange={(e) => setMonth(Number(e.target.value))}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
            {Array.from({ length: 12 }, (_, i) => (
              <option key={i + 1} value={i + 1}>
                {new Date(2024, i).toLocaleString('en', { month: 'long' })}
              </option>
            ))}
          </select>
          <select value={year} onChange={(e) => setYear(Number(e.target.value))}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
            {[today.getFullYear(), today.getFullYear() - 1].map((y) => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </div>
      </div>

      {profileLoading || loading ? (
        <div className="flex h-48 items-center justify-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        </div>
      ) : report ? (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {[
              { label: 'Present', value: report.presentDays, color: 'text-green-600' },
              { label: 'Absent', value: report.absentDays, color: 'text-red-600' },
              { label: 'Half Day', value: report.halfDays, color: 'text-yellow-600' },
              { label: 'On Leave', value: report.leaveDays, color: 'text-blue-600' },
            ].map((s) => (
              <div key={s.label} className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 text-center">
                <p className={`text-3xl font-bold ${s.color}`}>{s.value}</p>
                <p className="text-xs text-gray-500 mt-1">{s.label}</p>
              </div>
            ))}
          </div>

          <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Attendance Percentage</p>
              <p className="text-2xl font-bold text-gray-900">{report.attendancePercentage}%</p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Total Working Hours</p>
              <p className="text-2xl font-bold text-gray-900">{report.totalWorkingHours}</p>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-2">
            <Table
              keyField={(r) => r.id}
              data={report.dailyRecords}
              columns={[
                { header: 'Date', accessor: (r) => r.attendanceDate },
                { header: 'Check-in', accessor: (r) => r.checkInTime ? r.checkInTime.slice(11, 16) : '—' },
                { header: 'Check-out', accessor: (r) => r.checkOutTime ? r.checkOutTime.slice(11, 16) : '—' },
                { header: 'Hours', accessor: (r) => r.workingHours },
                { header: 'Status', accessor: (r) => <Badge color={statusColor[r.status]}>{r.status}</Badge> },
              ]}
            />
          </div>
        </>
      ) : (
        <p className="text-gray-400 text-sm">No attendance data for this period.</p>
      )}
    </div>
  );
}