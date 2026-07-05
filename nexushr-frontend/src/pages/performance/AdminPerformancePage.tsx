import { useEffect, useState } from 'react';
import { Star, Target, Plus, X, CheckCircle, XCircle, Users, TrendingUp, ClipboardList } from 'lucide-react';
import { performanceApi } from '../../api/performanceApi';
import { employeeApi } from '../../api/employeeApi';
import { useAuth } from '../../hooks/useAuth';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { StatCard } from '../../components/ui/StatCard';
import type { ReviewResponse, GoalResponse } from '../../types/performance';
import type { Employee } from '../../types/employee';

const QUARTERS = [
  { value: 1, label: 'Q1 (Jan – Mar)' },
  { value: 2, label: 'Q2 (Apr – Jun)' },
  { value: 3, label: 'Q3 (Jul – Sep)' },
  { value: 4, label: 'Q4 (Oct – Dec)' },
];

const CURRENT_YEAR = new Date().getFullYear();
const YEARS = [CURRENT_YEAR, CURRENT_YEAR - 1, CURRENT_YEAR + 1];

const ratingLabel = (r: number) => {
  if (r >= 4.5) return 'Outstanding';
  if (r >= 4.0) return 'Exceeds Expectations';
  if (r >= 3.0) return 'Meets Expectations';
  if (r >= 2.0) return 'Needs Improvement';
  return 'Unsatisfactory';
};

const ratingColor = (r: number): 'green' | 'blue' | 'yellow' | 'red' | 'gray' => {
  if (r >= 4.5) return 'green';
  if (r >= 4.0) return 'blue';
  if (r >= 3.0) return 'yellow';
  return 'red';
};

type Tab = 'overview' | 'review' | 'goal';

export function AdminPerformancePage() {
  const { user } = useAuth();
  const [tab, setTab] = useState<Tab>('overview');
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [allReviews, setAllReviews] = useState<ReviewResponse[]>([]);
  const [loadingReviews, setLoadingReviews] = useState(true);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; msg: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // Review form state
  const [rv, setRv] = useState({
    employeeId: '', reviewYear: CURRENT_YEAR, reviewQuarter: 3,
    rating: 4, feedback: '', strengths: '', improvements: '',
  });

  // Goal form state
  const [gl, setGl] = useState({
    employeeId: '', title: '', description: '',
    year: CURRENT_YEAR, quarter: 3, targetDate: '', status: 'IN_PROGRESS',
  });

  // Selected employee reviews for preview
  const [selectedEmpReviews, setSelectedEmpReviews] = useState<ReviewResponse[]>([]);

  const showToast = (type: 'success' | 'error', msg: string) => {
    setToast({ type, msg });
    setTimeout(() => setToast(null), 3500);
  };

  useEffect(() => {
    employeeApi.list({ size: 100 }).then(r => setEmployees(r.data.content ?? []));
  }, []);

  // Load reviews for all employees to build overview
  useEffect(() => {
    setLoadingReviews(true);
    // Get reviews for each employee in parallel
    const load = async () => {
      try {
        const results: ReviewResponse[] = [];
        for (const emp of employees.slice(0, 20)) {
          const r = await performanceApi.getReviews(emp.id);
          results.push(...r.data);
        }
        setAllReviews(results.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
      } finally {
        setLoadingReviews(false);
      }
    };
    if (employees.length > 0) load();
    else setLoadingReviews(false);
  }, [employees]);

  const handleEmployeeChangeForReview = async (empId: string) => {
    setRv(prev => ({ ...prev, employeeId: empId }));
    if (!empId) { setSelectedEmpReviews([]); return; }
    const r = await performanceApi.getReviews(empId);
    setSelectedEmpReviews(r.data);
  };

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rv.employeeId || !rv.feedback.trim()) return;
    setSubmitting(true);
    try {
      const submitted = await performanceApi.submitReview(user!.userId, {
        employeeId: rv.employeeId,
        reviewYear: rv.reviewYear,
        reviewQuarter: rv.reviewQuarter,
        rating: rv.rating,
        feedback: rv.feedback,
        strengths: rv.strengths || undefined,
        improvements: rv.improvements || undefined,
      });
      setAllReviews(prev => [submitted.data, ...prev]);
      setSelectedEmpReviews(prev => [submitted.data, ...prev]);
      setRv({ employeeId: rv.employeeId, reviewYear: CURRENT_YEAR, reviewQuarter: 3, rating: 4, feedback: '', strengths: '', improvements: '' });
      showToast('success', 'Performance review submitted successfully!');
    } catch {
      showToast('error', 'Failed to submit review. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmitGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!gl.employeeId || !gl.title.trim()) return;
    setSubmitting(true);
    try {
      await performanceApi.createGoal({
        employeeId: gl.employeeId,
        title: gl.title,
        description: gl.description || undefined,
        year: gl.year,
        quarter: gl.quarter,
        targetDate: gl.targetDate || undefined,
        status: gl.status,
      });
      setGl({ employeeId: gl.employeeId, title: '', description: '', year: CURRENT_YEAR, quarter: 3, targetDate: '', status: 'IN_PROGRESS' });
      showToast('success', 'Goal created successfully!');
    } catch {
      showToast('error', 'Failed to create goal. Target date must be today or in the future.');
    } finally {
      setSubmitting(false);
    }
  };

  const avgRating = allReviews.length > 0
    ? (allReviews.reduce((s, r) => s + r.rating, 0) / allReviews.length).toFixed(1)
    : '—';

  const tabBtn = (t: Tab, label: string, Icon: any) => (
    <button
      onClick={() => setTab(t)}
      className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-semibold transition-all ${
        tab === t ? 'bg-purple-600 text-white shadow-md' : 'text-gray-600 hover:bg-gray-100'
      }`}
    >
      <Icon className="w-4 h-4" /> {label}
    </button>
  );

  return (
    <div className="space-y-6">
      {/* Toast */}
      {toast && (
        <div className={`fixed top-6 right-6 z-50 flex items-center gap-3 px-5 py-3 rounded-xl shadow-xl text-white text-sm font-medium transition-all ${
          toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'
        }`}>
          {toast.type === 'success' ? <CheckCircle className="w-4 h-4" /> : <XCircle className="w-4 h-4" />}
          {toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Performance Management</h1>
          <p className="text-gray-500 mt-1 text-sm">Review, rate, and set goals for your workforce</p>
        </div>
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <StatCard
          title="Total Reviews"
          value={allReviews.length}
          icon={ClipboardList}
          color="bg-purple-500"
          accent="border-purple-500"
          sub="All submitted"
        />
        <StatCard
          title="Avg Rating"
          value={`${avgRating} / 5.0`}
          icon={Star}
          color="bg-amber-500"
          accent="border-amber-500"
          sub="Company-wide average"
        />
        <StatCard
          title="Employees"
          value={employees.length}
          icon={Users}
          color="bg-blue-500"
          accent="border-blue-500"
          sub="With active records"
        />
      </div>

      {/* Tab Nav */}
      <div className="flex gap-2 bg-gray-50 p-1.5 rounded-2xl w-fit">
        {tabBtn('overview', 'All Reviews', ClipboardList)}
        {tabBtn('review', 'Submit Review', Star)}
        {tabBtn('goal', 'Assign Goal', Target)}
      </div>

      {/* ── Overview Tab ─────────────────────────────────── */}
      {tab === 'overview' && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">All Performance Reviews</h2>
            <span className="text-sm text-gray-400">{allReviews.length} total</span>
          </div>
          {loadingReviews ? (
            <div className="flex items-center justify-center h-40">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-purple-500 border-t-transparent" />
            </div>
          ) : allReviews.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-48 text-gray-400">
              <ClipboardList className="w-10 h-10 mb-3 text-gray-300" />
              <p className="text-sm font-medium">No reviews submitted yet</p>
              <p className="text-xs mt-1">Switch to "Submit Review" tab to get started</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-50">
              {allReviews.map(r => (
                <div key={r.id} className="flex items-start justify-between px-6 py-4 hover:bg-gray-50 transition-colors">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-3 mb-1">
                      <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center shrink-0">
                        <span className="text-purple-700 font-bold text-xs">
                          {r.employeeName?.charAt(0).toUpperCase()}
                        </span>
                      </div>
                      <div>
                        <p className="text-sm font-semibold text-gray-900">{r.employeeName}</p>
                        <p className="text-xs text-gray-400">{r.employeeCode} · {r.reviewPeriod}</p>
                      </div>
                    </div>
                    <p className="text-sm text-gray-600 ml-11 line-clamp-2">{r.feedback}</p>
                    {r.strengths && (
                      <p className="text-xs text-green-600 ml-11 mt-1">💪 {r.strengths}</p>
                    )}
                  </div>
                  <div className="ml-4 flex flex-col items-end gap-1 shrink-0">
                    <Badge color={ratingColor(r.rating)}>
                      ⭐ {r.rating.toFixed(1)}
                    </Badge>
                    <span className="text-xs text-gray-400">{ratingLabel(r.rating)}</span>
                    <span className="text-xs text-gray-300">by {r.reviewerName}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ── Submit Review Tab ─────────────────────────────── */}
      {tab === 'review' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="font-semibold text-gray-900 mb-5 flex items-center gap-2">
              <Star className="w-5 h-5 text-amber-500" />
              Submit Performance Review
            </h2>
            <form onSubmit={handleSubmitReview} className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Employee *</label>
                <select
                  value={rv.employeeId}
                  onChange={e => handleEmployeeChangeForReview(e.target.value)}
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                  required
                >
                  <option value="">Select employee…</option>
                  {employees.map(e => (
                    <option key={e.id} value={e.id}>{e.firstName} {e.lastName} ({e.employeeCode})</option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Year *</label>
                  <select value={rv.reviewYear} onChange={e => setRv(p => ({ ...p, reviewYear: +e.target.value }))}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-purple-500">
                    {YEARS.map(y => <option key={y} value={y}>{y}</option>)}
                  </select>
                </div>
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Quarter *</label>
                  <select value={rv.reviewQuarter} onChange={e => setRv(p => ({ ...p, reviewQuarter: +e.target.value }))}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-purple-500">
                    {QUARTERS.map(q => <option key={q.value} value={q.value}>{q.label}</option>)}
                  </select>
                </div>
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-2">
                  Rating: <span className="text-purple-600 normal-case">{rv.rating.toFixed(1)} — {ratingLabel(rv.rating)}</span>
                </label>
                <div className="flex items-center gap-3">
                  <span className="text-xs text-gray-400">1.0</span>
                  <input type="range" min={1} max={5} step={0.1} value={rv.rating}
                    onChange={e => setRv(p => ({ ...p, rating: +e.target.value }))}
                    className="flex-1 h-2 accent-purple-600 cursor-pointer"
                  />
                  <span className="text-xs text-gray-400">5.0</span>
                </div>
                <div className="flex justify-between mt-1">
                  {[1, 2, 3, 4, 5].map(n => (
                    <Star key={n} className={`w-5 h-5 ${rv.rating >= n ? 'text-amber-400 fill-amber-400' : 'text-gray-200'}`} />
                  ))}
                </div>
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Feedback * <span className="text-gray-300 normal-case font-normal">(min 20 chars)</span></label>
                <textarea value={rv.feedback} onChange={e => setRv(p => ({ ...p, feedback: e.target.value }))}
                  rows={4} placeholder="Describe the employee's performance this quarter…"
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm resize-none focus:outline-none focus:ring-2 focus:ring-purple-500"
                  required minLength={20}
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Key Strengths <span className="text-gray-300 normal-case font-normal">(optional)</span></label>
                <input value={rv.strengths} onChange={e => setRv(p => ({ ...p, strengths: e.target.value }))}
                  placeholder="e.g. Problem-solving, leadership, communication…"
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Areas to Improve <span className="text-gray-300 normal-case font-normal">(optional)</span></label>
                <input value={rv.improvements} onChange={e => setRv(p => ({ ...p, improvements: e.target.value }))}
                  placeholder="e.g. Time management, documentation…"
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <Button type="submit" loading={submitting} className="w-full justify-center">
                <Star className="w-4 h-4" /> Submit Review
              </Button>
            </form>
          </div>

          {/* Employee's past reviews preview */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="font-semibold text-gray-900 mb-4">
              {rv.employeeId ? 'Previous Reviews for this Employee' : 'Select an employee to see past reviews'}
            </h2>
            {!rv.employeeId ? (
              <div className="flex flex-col items-center justify-center h-48 text-gray-300">
                <Users className="w-10 h-10 mb-2" />
                <p className="text-sm">No employee selected</p>
              </div>
            ) : selectedEmpReviews.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-48 text-gray-400">
                <ClipboardList className="w-8 h-8 mb-2 text-gray-300" />
                <p className="text-sm">No reviews yet for this employee</p>
              </div>
            ) : (
              <div className="space-y-3 max-h-[500px] overflow-y-auto pr-1">
                {selectedEmpReviews.map(r => (
                  <div key={r.id} className="border border-gray-100 rounded-xl p-4">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-semibold text-gray-900">{r.reviewPeriod}</span>
                      <Badge color={ratingColor(r.rating)}>⭐ {r.rating.toFixed(1)}</Badge>
                    </div>
                    <p className="text-sm text-gray-600">{r.feedback}</p>
                    {r.strengths && <p className="text-xs text-green-600 mt-1.5">💪 {r.strengths}</p>}
                    {r.improvements && <p className="text-xs text-amber-600 mt-1">🎯 {r.improvements}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── Assign Goal Tab ───────────────────────────────── */}
      {tab === 'goal' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="font-semibold text-gray-900 mb-5 flex items-center gap-2">
              <Target className="w-5 h-5 text-blue-500" />
              Assign Performance Goal
            </h2>
            <form onSubmit={handleSubmitGoal} className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Employee *</label>
                <select value={gl.employeeId} onChange={e => setGl(p => ({ ...p, employeeId: e.target.value }))}
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                  <option value="">Select employee…</option>
                  {employees.map(e => (
                    <option key={e.id} value={e.id}>{e.firstName} {e.lastName} ({e.employeeCode})</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Goal Title *</label>
                <input value={gl.title} onChange={e => setGl(p => ({ ...p, title: e.target.value }))}
                  placeholder="e.g. Complete Q3 product roadmap…"
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required minLength={5}
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Description <span className="text-gray-300 normal-case font-normal">(optional)</span></label>
                <textarea value={gl.description} onChange={e => setGl(p => ({ ...p, description: e.target.value }))}
                  rows={3} placeholder="Describe success criteria and milestones…"
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Year *</label>
                  <select value={gl.year} onChange={e => setGl(p => ({ ...p, year: +e.target.value }))}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    {YEARS.map(y => <option key={y} value={y}>{y}</option>)}
                  </select>
                </div>
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Quarter *</label>
                  <select value={gl.quarter} onChange={e => setGl(p => ({ ...p, quarter: +e.target.value }))}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    {QUARTERS.map(q => <option key={q.value} value={q.value}>{q.label}</option>)}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Target Date <span className="text-gray-300 font-normal">(optional)</span></label>
                  <input type="date" value={gl.targetDate} min={new Date().toISOString().split('T')[0]}
                    onChange={e => setGl(p => ({ ...p, targetDate: e.target.value }))}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide block mb-1">Initial Status</label>
                  <select value={gl.status} onChange={e => setGl(p => ({ ...p, status: e.target.value }))}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="CANCELLED">Cancelled</option>
                  </select>
                </div>
              </div>

              <Button type="submit" loading={submitting} className="w-full justify-center">
                <Plus className="w-4 h-4" /> Create Goal
              </Button>
            </form>
          </div>

          {/* Tips panel */}
          <div className="bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl border border-blue-100 p-6">
            <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-blue-500" />
              Best Practices
            </h3>
            <ul className="space-y-3">
              {[
                { icon: '🎯', title: 'SMART Goals', desc: 'Set Specific, Measurable, Achievable, Relevant, Time-bound goals.' },
                { icon: '📅', title: 'Quarterly Cadence', desc: 'Align goals with quarters so progress can be measured at review time.' },
                { icon: '⭐', title: 'Regular Reviews', desc: 'Submit reviews at the end of each quarter for all direct reports.' },
                { icon: '💬', title: 'Specific Feedback', desc: 'Cite real examples in feedback — avoid vague praise or criticism.' },
                { icon: '📈', title: 'Growth Focus', desc: 'Balance strengths recognition with actionable improvement areas.' },
              ].map(tip => (
                <li key={tip.title} className="flex gap-3">
                  <span className="text-xl shrink-0">{tip.icon}</span>
                  <div>
                    <p className="text-sm font-semibold text-gray-800">{tip.title}</p>
                    <p className="text-xs text-gray-500">{tip.desc}</p>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
}
