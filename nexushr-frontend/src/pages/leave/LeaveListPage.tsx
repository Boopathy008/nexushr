import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, X, CheckCircle, XCircle } from 'lucide-react';
import { leaveApi } from '../../api/leaveApi';
import { useEmployeeProfile } from '../../hooks/useEmployeeProfile';
import { useAuth } from '../../hooks/useAuth';
import { Table } from '../../components/ui/Table';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Pagination } from '../../components/ui/Pagination';
import type { LeaveResponse, LeaveStatus } from '../../types/leave';

const statusColor: Record<LeaveStatus, 'green' | 'red' | 'yellow' | 'gray'> = {
  PENDING: 'yellow', APPROVED: 'green', REJECTED: 'red', CANCELLED: 'gray',
};

interface RejectModal {
  leaveId: string;
  note: string;
}

export function LeaveListPage() {
  const { employeeId, loading: profileLoading } = useEmployeeProfile();
  const { hasAnyRole, user } = useAuth();
  const [leaves, setLeaves] = useState<LeaveResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState<'mine' | 'pending'>('mine');
  const [rejectModal, setRejectModal] = useState<RejectModal | null>(null);
  const [deciding, setDeciding] = useState(false);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const showToast = (type: 'success' | 'error', message: string) => {
    setToast({ type, message });
    setTimeout(() => setToast(null), 3000);
  };

  const fetchMine = () => {
    if (!employeeId) {
      setLoading(false);
      setLeaves([]);
      setTotalPages(0);
      return;
    }
    setLoading(true);
    leaveApi.getHistory(employeeId, page, 10)
      .then((r) => { setLeaves(r.data.content); setTotalPages(r.data.totalPages); })
      .finally(() => setLoading(false));
  };

  const fetchPending = () => {
    setLoading(true);
    leaveApi.getPending(page, 10)
      .then((r) => { setLeaves(r.data.content); setTotalPages(r.data.totalPages); })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (viewMode === 'mine') fetchMine();
    else fetchPending();
  }, [employeeId, page, viewMode]);

  const handleCancel = async (id: string) => {
    if (!employeeId) return;
    try {
      await leaveApi.cancel(id, employeeId);
      fetchMine();
      showToast('success', 'Leave cancelled successfully');
    } catch {
      showToast('error', 'Failed to cancel leave');
    }
  };

  const handleApprove = async (id: string) => {
    const managerId = user?.userId;
    if (!managerId) { showToast('error', 'Manager profile not linked.'); return; }
    setDeciding(true);
    try {
      await leaveApi.decide(id, managerId, { decision: 'APPROVED' });
      setLeaves(prev => prev.filter(l => l.id !== id));
      showToast('success', 'Leave approved successfully');
    } catch {
      showToast('error', 'Failed to approve leave');
    } finally {
      setDeciding(false);
    }
  };

  const handleRejectSubmit = async () => {
    if (!rejectModal) return;
    const managerId = user?.userId;
    if (!managerId) { showToast('error', 'Manager profile not linked.'); return; }
    if (!rejectModal.note.trim()) return;
    setDeciding(true);
    try {
      await leaveApi.decide(rejectModal.leaveId, managerId, {
        decision: 'REJECTED',
        rejectionNote: rejectModal.note,
      });
      setLeaves(prev => prev.filter(l => l.id !== rejectModal.leaveId));
      setRejectModal(null);
      showToast('success', 'Leave rejected successfully');
    } catch {
      showToast('error', 'Failed to reject leave');
    } finally {
      setDeciding(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Toast */}
      {toast && (
        <div className={`fixed top-6 right-6 z-50 flex items-center gap-3 px-5 py-3 rounded-xl shadow-lg text-white text-sm font-medium transition-all ${
          toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'
        }`}>
          {toast.type === 'success'
            ? <CheckCircle className="w-4 h-4" />
            : <XCircle className="w-4 h-4" />}
          {toast.message}
        </div>
      )}

      {/* Reject Modal */}
      {rejectModal && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md mx-4">
            <h2 className="text-lg font-semibold text-gray-900 mb-1">Reject Leave Request</h2>
            <p className="text-sm text-gray-500 mb-4">Please provide a reason for rejecting this leave request.</p>
            <textarea
              value={rejectModal.note}
              onChange={(e) => setRejectModal({ ...rejectModal, note: e.target.value })}
              rows={4}
              placeholder="Enter rejection reason..."
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-red-400 resize-none"
            />
            {!rejectModal.note.trim() && (
              <p className="text-xs text-red-500 mt-1">Rejection reason is required.</p>
            )}
            <div className="flex gap-3 mt-4">
              <button
                onClick={handleRejectSubmit}
                disabled={deciding || !rejectModal.note.trim()}
                className="flex-1 bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white text-sm font-medium py-2.5 rounded-lg transition"
              >
                {deciding ? 'Rejecting...' : 'Confirm Rejection'}
              </button>
              <button
                onClick={() => setRejectModal(null)}
                className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-medium py-2.5 rounded-lg transition"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Leave Management</h1>
          <p className="text-gray-500 mt-1">View and manage leave requests</p>
        </div>
        <div className="flex gap-3">
          {hasAnyRole('ADMIN', 'MANAGER') && (
            <div className="flex rounded-lg border border-gray-200 overflow-hidden">
              <button onClick={() => { setViewMode('mine'); setPage(0); }}
                className={`px-3 py-1.5 text-sm ${viewMode === 'mine' ? 'bg-blue-600 text-white' : 'bg-white text-gray-600'}`}>
                My Leaves
              </button>
              <button onClick={() => { setViewMode('pending'); setPage(0); }}
                className={`px-3 py-1.5 text-sm ${viewMode === 'pending' ? 'bg-blue-600 text-white' : 'bg-white text-gray-600'}`}>
                Pending Approvals
              </button>
            </div>
          )}
          <Link to="/leaves/apply">
            <Button><Plus className="w-4 h-4" /> Apply Leave</Button>
          </Link>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-2">
        {profileLoading || loading ? (
          <div className="h-48 flex items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
          </div>
        ) : (
          <>
            <Table
              keyField={(l) => l.id}
              data={leaves}
              columns={[
                ...(viewMode === 'pending' ? [{ header: 'Employee', accessor: (l: LeaveResponse) => l.employeeName }] : []),
                { header: 'Type', accessor: (l) => l.leaveTypeName },
                { header: 'From', accessor: (l) => l.startDate },
                { header: 'To', accessor: (l) => l.endDate },
                { header: 'Days', accessor: (l) => l.totalDays },
                { header: 'Status', accessor: (l) => <Badge color={statusColor[l.status]}>{l.status}</Badge> },
                { header: 'Actions', accessor: (l) => (
                    <div className="flex gap-2">
                      {viewMode === 'mine' && l.status === 'PENDING' && (
                        <button onClick={() => handleCancel(l.id)} className="text-red-500 hover:text-red-700 text-xs font-medium flex items-center gap-1">
                          <X className="w-3 h-3" /> Cancel
                        </button>
                      )}
                      {viewMode === 'pending' && l.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => handleApprove(l.id)}
                            disabled={deciding}
                            className="text-green-600 hover:text-green-800 text-xs font-medium disabled:opacity-50"
                          >
                            Approve
                          </button>
                          <button
                            onClick={() => setRejectModal({ leaveId: l.id, note: '' })}
                            disabled={deciding}
                            className="text-red-500 hover:text-red-700 text-xs font-medium disabled:opacity-50"
                          >
                            Reject
                          </button>
                        </>
                      )}
                    </div>
                  ) },
              ]}
              emptyMessage="No leave requests found"
            />
            <div className="px-4 pb-2">
              <Pagination page={page} totalPages={totalPages} onChange={setPage} />
            </div>
          </>
        )}
      </div>
    </div>
  );
}
