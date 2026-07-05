import { useEffect, useState } from 'react';
import { performanceApi } from '../../api/performanceApi';
import { useEmployeeProfile } from '../../hooks/useEmployeeProfile';
import { useAuth } from '../../hooks/useAuth';
import { Badge } from '../../components/ui/Badge';
import { AdminPerformancePage } from './AdminPerformancePage';
import type { ReviewResponse, GoalResponse } from '../../types/performance';

export function PerformancePage() {
  const { hasAnyRole } = useAuth();

  // Admin and Manager → full management view
  if (hasAnyRole('ADMIN', 'MANAGER')) {
    return <AdminPerformancePage />;
  }

  // Employee → personal view
  return <EmployeePerformanceView />;
}

function EmployeePerformanceView() {
  const { employeeId, loading: profileLoading } = useEmployeeProfile();
  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [goals, setGoals] = useState<GoalResponse[]>([]);
  const [avgRating, setAvgRating] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchPerformanceData = () => {
    if (!employeeId) { setLoading(false); return; }
    setLoading(true);
    Promise.all([
      performanceApi.getReviews(employeeId),
      performanceApi.getGoals(employeeId),
      performanceApi.getAverageRating(employeeId),
    ]).then(([r, g, avg]) => {
      setReviews(r.data);
      setGoals(g.data);
      setAvgRating(avg.data);
    }).finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchPerformanceData();
  }, [employeeId]);

  const handleCompleteGoal = async (goalId: string) => {
    try {
      await performanceApi.updateGoalStatus(goalId, 'COMPLETED');
      fetchPerformanceData(); // Refresh the list
    } catch (e) {
      console.error('Failed to complete goal', e);
      alert('Failed to mark goal as complete.');
    }
  };

  if (profileLoading || loading) return (
    <div className="flex h-64 items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
    </div>
  );

  if (!employeeId) {
    return (
      <div className="p-6 bg-amber-50 border border-amber-200 rounded-xl text-amber-800 text-sm">
        No employee profile is linked to your account. Performance data cannot be displayed.
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Performance</h1>
          <p className="text-gray-500 mt-1">Track your goals and review history</p>
        </div>
        <div className="bg-gradient-to-br from-indigo-50 to-purple-50 rounded-xl px-6 py-4 shadow-inner border border-indigo-100/50 text-center">
          <p className="text-xs font-semibold text-indigo-900 uppercase tracking-wider mb-1">Average Rating</p>
          <div className="flex items-baseline justify-center gap-1">
            <span className="text-3xl font-extrabold text-indigo-700">{avgRating.toFixed(1)}</span>
            <span className="text-sm font-medium text-indigo-400">/ 5.0</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Goals Section */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col h-full">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-bold text-gray-900">Current Tasks & Goals</h2>
            <Badge color="blue">{goals.length} Total</Badge>
          </div>
          
          {goals.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-8 bg-gray-50 rounded-xl border border-dashed border-gray-200">
              <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center shadow-sm mb-3">
                <span className="text-xl">🎯</span>
              </div>
              <p className="font-medium text-gray-900">No active goals</p>
              <p className="text-sm text-gray-500 mt-1">You don't have any tasks assigned yet.</p>
            </div>
          ) : (
            <div className="space-y-4 overflow-y-auto pr-2" style={{ maxHeight: '600px' }}>
              {goals.map((g) => (
                <div key={g.id} className={`p-5 border rounded-xl transition-all ${g.status === 'COMPLETED' ? 'bg-green-50/30 border-green-100' : 'bg-white border-gray-100 hover:border-blue-200 hover:shadow-md'}`}>
                  <div className="flex justify-between items-start mb-3">
                    <h3 className={`font-semibold ${g.status === 'COMPLETED' ? 'text-green-800 line-through opacity-70' : 'text-gray-900'}`}>{g.title}</h3>
                    <Badge color={g.status === 'COMPLETED' ? 'green' : g.status === 'CANCELLED' ? 'gray' : 'blue'}>
                      {g.status.replace('_', ' ')}
                    </Badge>
                  </div>
                  
                  {g.description && (
                    <div className="mb-4 text-sm text-gray-600 bg-gray-50 p-3 rounded-lg border border-gray-100">
                      <p className="font-medium text-gray-700 mb-1 text-xs uppercase tracking-wider">Task Details:</p>
                      {g.description}
                    </div>
                  )}
                  
                  <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-50">
                    <div className="text-xs font-medium text-gray-500 flex items-center gap-2">
                      <span className="bg-gray-100 px-2 py-1 rounded text-gray-600">{g.quarterLabel}</span>
                      {g.targetDate && <span>Due: {g.targetDate}</span>}
                    </div>
                    
                    {g.status !== 'COMPLETED' && g.status !== 'CANCELLED' && (
                      <button
                        onClick={() => handleCompleteGoal(g.id)}
                        className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg shadow-sm transition-colors focus:ring-2 focus:ring-indigo-500 focus:ring-offset-1"
                      >
                        Mark Complete
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Review History Section */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col h-full">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-bold text-gray-900">Review History</h2>
            <Badge color="purple">{reviews.length} Reviews</Badge>
          </div>

          {reviews.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-8 bg-gray-50 rounded-xl border border-dashed border-gray-200">
              <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center shadow-sm mb-3">
                <span className="text-xl">📝</span>
              </div>
              <p className="font-medium text-gray-900">No reviews yet</p>
              <p className="text-sm text-gray-500 mt-1">Your manager hasn't submitted any performance reviews.</p>
            </div>
          ) : (
            <div className="space-y-4 overflow-y-auto pr-2" style={{ maxHeight: '600px' }}>
              {reviews.map((r) => (
                <div key={r.id} className="bg-white border border-gray-100 rounded-xl p-5 hover:border-purple-200 hover:shadow-md transition-all">
                  <div className="flex items-center justify-between mb-4 pb-4 border-b border-gray-50">
                    <div>
                      <p className="font-bold text-gray-900">{r.reviewPeriod}</p>
                      <p className="text-xs text-gray-500 mt-0.5">By {r.reviewerName}</p>
                    </div>
                    <div className="flex flex-col items-end gap-1">
                      <span className="inline-flex items-center justify-center px-3 py-1 bg-purple-100 text-purple-700 font-bold rounded-lg text-sm border border-purple-200">
                        {r.rating.toFixed(1)} / 5.0
                      </span>
                      <span className="text-[10px] font-bold text-purple-600 uppercase tracking-wider">{r.ratingLabel}</span>
                    </div>
                  </div>
                  
                  <div className="space-y-4">
                    <div>
                      <p className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Feedback</p>
                      <p className="text-sm text-gray-700 leading-relaxed bg-gray-50 p-3 rounded-lg">{r.feedback}</p>
                    </div>
                    
                    <div className="grid grid-cols-2 gap-3">
                      {r.strengths && (
                        <div className="bg-emerald-50/50 p-3 rounded-lg border border-emerald-100">
                          <p className="text-xs font-bold text-emerald-700 uppercase tracking-wider mb-1 flex items-center gap-1"><span>✨</span> Strengths</p>
                          <p className="text-sm text-emerald-900">{r.strengths}</p>
                        </div>
                      )}
                      {r.improvements && (
                        <div className="bg-amber-50/50 p-3 rounded-lg border border-amber-100">
                          <p className="text-xs font-bold text-amber-700 uppercase tracking-wider mb-1 flex items-center gap-1"><span>📈</span> Areas to Improve</p>
                          <p className="text-sm text-amber-900">{r.improvements}</p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}