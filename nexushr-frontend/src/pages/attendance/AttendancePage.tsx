import { useEffect, useState } from 'react';
import { Clock, LogIn, LogOut, Calendar } from 'lucide-react';
import { format } from 'date-fns';
import { Link } from 'react-router-dom';
import { attendanceApi } from '../../api/attendanceApi';
import { useEmployeeProfile } from '../../hooks/useEmployeeProfile';
import type { AttendanceResponse } from '../../types/attendance';

export function AttendancePage() {
  const { employeeId, loading: profileLoading } = useEmployeeProfile();
  const [record, setRecord] = useState<AttendanceResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!employeeId) { setLoading(false); return; }
    attendanceApi.getToday(employeeId)
      .then((r) => setRecord(r.data))
      .catch(() => setRecord(null))
      .finally(() => setLoading(false));
  }, [employeeId]);

  const handleCheckIn = async () => {
    if (!employeeId) return;
    setActionLoading(true); setError('');
    try {
      const r = await attendanceApi.checkIn(employeeId);
      setRecord(r.data);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'Check-in failed');
    } finally { setActionLoading(false); }
  };

  const handleCheckOut = async () => {
    if (!employeeId) return;
    setActionLoading(true); setError('');
    try {
      const r = await attendanceApi.checkOut(employeeId);
      setRecord(r.data);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'Check-out failed');
    } finally { setActionLoading(false); }
  };

  const now = new Date();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Attendance</h1>
        <p className="text-gray-500 mt-1">{format(now, 'EEEE, MMMM d yyyy')}</p>
      </div>

      {error && <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">{error}</div>}

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 max-w-md">
        <div className="flex items-center gap-3 mb-6">
          <div className="p-3 bg-blue-50 rounded-xl">
            <Clock className="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <p className="font-semibold text-gray-900">Today's Attendance</p>
            <p className="text-sm text-gray-500">{format(now, 'HH:mm:ss')}</p>
          </div>
        </div>

        {profileLoading || loading ? (
          <div className="h-24 flex items-center justify-center">
            <div className="h-6 w-6 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
          </div>
        ) : (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 bg-gray-50 rounded-xl">
                <p className="text-xs text-gray-500 mb-1">Check-in</p>
                <p className="font-semibold text-gray-900">
                  {record?.checkInTime ? format(new Date(record.checkInTime), 'HH:mm') : '—'}
                </p>
              </div>
              <div className="p-4 bg-gray-50 rounded-xl">
                <p className="text-xs text-gray-500 mb-1">Check-out</p>
                <p className="font-semibold text-gray-900">
                  {record?.checkOutTime ? format(new Date(record.checkOutTime), 'HH:mm') : '—'}
                </p>
              </div>
            </div>

            {record?.workingHours && (
              <div className="p-4 bg-blue-50 rounded-xl text-center">
                <p className="text-xs text-blue-600 font-medium mb-1">Working hours today</p>
                <p className="text-2xl font-bold text-blue-700">{record.workingHours}</p>
              </div>
            )}

            <div className="flex gap-3 pt-2">
              <button
                onClick={handleCheckIn}
                disabled={!!record?.checkInTime || actionLoading || !employeeId}
                className="flex-1 flex items-center justify-center gap-2 py-3 bg-green-600 hover:bg-green-700 disabled:bg-gray-200 disabled:text-gray-400 text-white font-semibold rounded-xl transition text-sm"
              >
                <LogIn className="w-4 h-4" /> Check In
              </button>
              <button
                onClick={handleCheckOut}
                disabled={!record?.checkInTime || !!record?.checkOutTime || actionLoading || !employeeId}
                className="flex-1 flex items-center justify-center gap-2 py-3 bg-red-500 hover:bg-red-600 disabled:bg-gray-200 disabled:text-gray-400 text-white font-semibold rounded-xl transition text-sm"
              >
                <LogOut className="w-4 h-4" /> Check Out
              </button>
            </div>
          </div>
        )}
      </div>

      <Link to="/attendance/report" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-800 text-sm font-medium">
        <Calendar className="w-4 h-4" /> View monthly report →
      </Link>
    </div>
  );
}