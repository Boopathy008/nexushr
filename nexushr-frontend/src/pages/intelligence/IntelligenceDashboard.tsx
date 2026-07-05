import { useState } from 'react';
import { Brain, AlertTriangle, TrendingUp, Search } from 'lucide-react';
import api from '../../api/axios';
import type { WorkforceIntelligenceResponse } from '../../types/intelligence';

const riskColors: Record<string, string> = {
  LOW: 'bg-green-100 text-green-800',
  MEDIUM: 'bg-yellow-100 text-yellow-800',
  HIGH: 'bg-orange-100 text-orange-800',
  CRITICAL: 'bg-red-100 text-red-800',
};

const engageColors: Record<string, string> = {
  HIGHLY_ENGAGED: 'bg-blue-100 text-blue-800',
  ENGAGED: 'bg-green-100 text-green-800',
  MODERATE: 'bg-yellow-100 text-yellow-800',
  DISENGAGED: 'bg-red-100 text-red-800',
};

export function IntelligenceDashboard() {
  const [employeeId, setEmployeeId] = useState('');
  const [data, setData] = useState<WorkforceIntelligenceResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const analyze = async () => {
    if (!employeeId.trim()) return;
    setLoading(true); setError(''); setData(null);
    try {
      let targetId = employeeId.trim();
      
      // If it doesn't look like a UUID, search for the employee first
      if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(targetId)) {
        const searchRes = await api.get('/employees', { params: { search: targetId } });
        if (searchRes.data?.content?.length > 0) {
          targetId = searchRes.data.content[0].id;
        } else {
          throw new Error('Employee not found');
        }
      }

      const r = await api.get<WorkforceIntelligenceResponse>(`/intelligence/employee/${targetId}`);
      setData(r.data);
    } catch {
      setError('Could not load intelligence report. Check the employee ID or Code.');
    } finally { setLoading(false); }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <div className="p-3 bg-purple-100 rounded-xl">
          <Brain className="w-6 h-6 text-purple-600" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">AI Workforce Intelligence</h1>
          <p className="text-gray-500 text-sm">Rule-based attrition risk and engagement analysis</p>
        </div>
      </div>

      <div className="flex gap-3 max-w-lg">
        <input
          value={employeeId}
          onChange={(e) => setEmployeeId(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && analyze()}
          placeholder="Paste employee UUID…"
          className="flex-1 px-4 py-2.5 border border-gray-300 rounded-xl text-sm outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
        />
        <button onClick={analyze} disabled={loading}
          className="px-5 py-2.5 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-300 text-white font-semibold rounded-xl transition text-sm flex items-center gap-2">
          <Search className="w-4 h-4" />
          {loading ? 'Analysing…' : 'Analyse'}
        </button>
      </div>

      {error && <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">{error}</div>}

      {data && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="lg:col-span-2 bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-start justify-between">
              <div>
                <p className="text-xl font-bold text-gray-900">{data.employeeName}</p>
                <p className="text-gray-500 text-sm">{data.employeeCode} · {data.designation} · {data.department}</p>
                <p className="text-gray-400 text-xs mt-1">Tenure: {data.tenureLabel}</p>
              </div>
              <div className="flex gap-2 flex-wrap justify-end">
                <span className={`px-3 py-1 rounded-full text-xs font-semibold ${riskColors[data.attritionRiskLevel] ?? ''}`}>
                  Risk: {data.attritionRiskLevel}
                </span>
                <span className={`px-3 py-1 rounded-full text-xs font-semibold ${engageColors[data.engagementLevel] ?? ''}`}>
                  {data.engagementLevel.replace('_', ' ')}
                </span>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
            <h2 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-red-500" />
              Attrition Risk — {data.attritionRiskScore}/100
            </h2>
            <div className="mb-4">
              <div className="h-2.5 bg-gray-100 rounded-full overflow-hidden">
                <div className={`h-full rounded-full transition-all ${
                  data.attritionRiskScore >= 70 ? 'bg-red-500' :
                  data.attritionRiskScore >= 45 ? 'bg-orange-400' :
                  data.attritionRiskScore >= 20 ? 'bg-yellow-400' : 'bg-green-400'
                }`} style={{ width: `${data.attritionRiskScore}%` }} />
              </div>
            </div>
            <ul className="space-y-2">
              {data.attritionRiskFactors.map((f, i) => (
                <li key={i} className="text-sm text-gray-600 flex gap-2">
                  <span className="text-red-400 shrink-0">•</span>{f}
                </li>
              ))}
            </ul>
          </div>

          <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
            <h2 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-blue-500" />
              Engagement — {data.engagementScore}/100
            </h2>
            <div className="mb-4">
              <div className="h-2.5 bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full rounded-full bg-blue-500 transition-all" style={{ width: `${data.engagementScore}%` }} />
              </div>
            </div>
            <ul className="space-y-2">
              {data.engagementFactors.map((f, i) => (
                <li key={i} className="text-sm text-gray-600 flex gap-2">
                  <span className="text-blue-400 shrink-0">•</span>{f}
                </li>
              ))}
            </ul>
          </div>

          <div className="lg:col-span-2 bg-gradient-to-r from-purple-50 to-blue-50 rounded-2xl p-6 border border-purple-100">
            <h2 className="font-semibold text-gray-900 mb-4">Recommendations</h2>
            <ul className="space-y-2">
              {data.recommendations.map((r, i) => (
                <li key={i} className="flex gap-3 text-sm text-gray-700">
                  <span className="text-purple-500 font-bold shrink-0">{i + 1}.</span>{r}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
}