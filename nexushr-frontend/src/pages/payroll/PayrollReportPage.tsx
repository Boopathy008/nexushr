import { useState } from 'react';
import { payrollApi } from '../../api/payrollApi';
import { Table } from '../../components/ui/Table';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import type { PayslipResponse } from '../../types/payroll';

export function PayrollReportPage() {
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [records, setRecords] = useState<PayslipResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);

  const fetchPeriod = async () => {
    setLoading(true);
    try {
      const r = await import('../../api/axios').then((m) =>
        m.default.get<PayslipResponse[]>('/payroll/period', { params: { month, year } }));
      setRecords(r.data);
    } finally { setLoading(false); }
  };

  const handleBulkGenerate = async () => {
    setGenerating(true);
    try {
      await payrollApi.bulkGenerate(month, year);
      await fetchPeriod();
    } finally { setGenerating(false); }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Payroll Reports</h1>
        <div className="flex gap-2">
          <select value={month} onChange={(e) => setMonth(Number(e.target.value))}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
            {Array.from({ length: 12 }, (_, i) => (
              <option key={i + 1} value={i + 1}>{new Date(2024, i).toLocaleString('en', { month: 'long' })}</option>
            ))}
          </select>
          <select value={year} onChange={(e) => setYear(Number(e.target.value))}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
            {[today.getFullYear(), today.getFullYear() - 1].map((y) => <option key={y} value={y}>{y}</option>)}
          </select>
          <Button variant="secondary" onClick={fetchPeriod} loading={loading}>Load</Button>
          <Button onClick={handleBulkGenerate} loading={generating}>Bulk Generate</Button>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-2">
        <Table
          keyField={(r) => r.id}
          data={records}
          columns={[
            { header: 'Employee', accessor: (r) => r.employeeName },
            { header: 'Code', accessor: (r) => r.employeeCode },
            { header: 'Department', accessor: (r) => r.department },
            { header: 'Gross', accessor: (r) => `₹${r.grossSalary.toLocaleString('en-IN')}` },
            { header: 'Deductions', accessor: (r) => `₹${r.totalDeductions.toLocaleString('en-IN')}` },
            { header: 'Net Salary', accessor: (r) => `₹${r.netSalary.toLocaleString('en-IN')}` },
            { header: 'Status', accessor: (r) => <Badge color={r.status === 'PROCESSED' ? 'green' : 'yellow'}>{r.status}</Badge> },
          ]}
          emptyMessage="No payroll records loaded. Click Load or Bulk Generate."
        />
      </div>
    </div>
  );
}