import { useEffect, useState } from 'react';
import { payrollApi } from '../../api/payrollApi';
import { useEmployeeProfile } from '../../hooks/useEmployeeProfile';
import { Badge } from '../../components/ui/Badge';
import type { PayslipResponse } from '../../types/payroll';

export function PayslipPage() {
  const { employeeId, loading: profileLoading } = useEmployeeProfile();
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [payslip, setPayslip] = useState<PayslipResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!employeeId) { setLoading(false); return; }
    setLoading(true); setError('');
    payrollApi.getPayslip(employeeId, month, year)
      .then((r) => setPayslip(r.data))
      .catch(() => { setPayslip(null); setError('No payslip found for this period.'); })
      .finally(() => setLoading(false));
  }, [employeeId, month, year]);

  const row = (label: string, value: number, positive = true) => (
    <div className="flex justify-between py-2 border-b border-gray-50 last:border-0">
      <span className="text-sm text-gray-600">{label}</span>
      <span className={`text-sm font-semibold ${positive ? 'text-gray-900' : 'text-red-600'}`}>
        {positive ? '' : '−'}₹{value.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
      </span>
    </div>
  );

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between bg-white p-6 rounded-2xl shadow-sm border border-gray-100 gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Payslip</h1>
          <p className="text-gray-500 mt-1">View your monthly salary details and deductions</p>
        </div>
        <div className="flex gap-3 bg-gray-50 p-2 rounded-xl border border-gray-100">
          <select value={month} onChange={(e) => setMonth(Number(e.target.value))}
            className="px-4 py-2.5 bg-white border border-gray-200 rounded-lg text-sm font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 shadow-sm cursor-pointer hover:border-indigo-300 transition-colors">
            {Array.from({ length: 12 }, (_, i) => (
              <option key={i + 1} value={i + 1}>{new Date(2024, i).toLocaleString('en', { month: 'long' })}</option>
            ))}
          </select>
          <select value={year} onChange={(e) => setYear(Number(e.target.value))}
            className="px-4 py-2.5 bg-white border border-gray-200 rounded-lg text-sm font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 shadow-sm cursor-pointer hover:border-indigo-300 transition-colors">
            {[today.getFullYear(), today.getFullYear() - 1].map((y) => <option key={y} value={y}>{y}</option>)}
          </select>
        </div>
      </div>

      {profileLoading || loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent" />
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center text-center p-12 bg-white rounded-2xl shadow-sm border border-gray-100 h-64">
          <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4">
            <span className="text-3xl text-gray-400">📄</span>
          </div>
          <p className="text-lg font-bold text-gray-900">No Payslip Available</p>
          <p className="text-gray-500 mt-1">There is no processed payroll data for {new Date(2024, month - 1).toLocaleString('en', { month: 'long' })} {year}.</p>
        </div>
      ) : payslip ? (
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-slate-900 to-indigo-950 px-8 py-6 text-white flex justify-between items-start">
            <div>
              <h2 className="text-3xl font-bold tracking-tight">{payslip.employeeName}</h2>
              <div className="flex items-center gap-2 mt-2 text-indigo-200 font-medium text-sm">
                <span>{payslip.employeeCode}</span>
                <span className="w-1.5 h-1.5 rounded-full bg-indigo-500"></span>
                <span>{payslip.designation}</span>
                <span className="w-1.5 h-1.5 rounded-full bg-indigo-500"></span>
                <span>{payslip.department}</span>
              </div>
            </div>
            <div className="text-right">
              <p className="text-indigo-200 text-sm font-medium uppercase tracking-wider mb-1">Pay Period</p>
              <p className="text-xl font-bold">{payslip.payPeriod}</p>
              <div className="mt-2 inline-block">
                <Badge color={payslip.status === 'PROCESSED' || payslip.status === 'PAID' ? 'green' : 'yellow'}>{payslip.status}</Badge>
              </div>
            </div>
          </div>

          <div className="p-8">
            {/* Attendance Summary */}
            <div className="grid grid-cols-3 gap-4 mb-8">
              <div className="bg-indigo-50/50 rounded-xl p-4 border border-indigo-100 flex flex-col items-center justify-center text-center">
                <p className="text-xs font-bold text-indigo-400 uppercase tracking-wider mb-1">Working Days</p>
                <p className="text-2xl font-black text-indigo-950">{payslip.workingDays}</p>
              </div>
              <div className="bg-emerald-50/50 rounded-xl p-4 border border-emerald-100 flex flex-col items-center justify-center text-center">
                <p className="text-xs font-bold text-emerald-500 uppercase tracking-wider mb-1">Present</p>
                <p className="text-2xl font-black text-emerald-900">{payslip.presentDays}</p>
              </div>
              <div className="bg-rose-50/50 rounded-xl p-4 border border-rose-100 flex flex-col items-center justify-center text-center">
                <p className="text-xs font-bold text-rose-400 uppercase tracking-wider mb-1">Loss of Pay</p>
                <p className="text-2xl font-black text-rose-700">{payslip.lopDays}</p>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 lg:gap-12">
              {/* Earnings */}
              <div>
                <h3 className="text-sm font-bold text-gray-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-emerald-500"></span> Earnings
                </h3>
                <div className="space-y-1">
                  {row('Basic Salary', payslip.basicSalary)}
                  {row('HRA', payslip.hra)}
                  {row('Allowances', payslip.allowances)}
                </div>
                <div className="flex justify-between items-center py-4 mt-2 border-t-2 border-gray-100">
                  <span className="text-base font-bold text-gray-900">Gross Earnings</span>
                  <span className="text-lg font-bold text-gray-900">₹{payslip.grossSalary.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                </div>
              </div>

              {/* Deductions */}
              <div>
                <h3 className="text-sm font-bold text-gray-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-rose-500"></span> Deductions
                </h3>
                <div className="space-y-1">
                  {row('Tax Deduction', payslip.taxDeduction, false)}
                  {row('PF Deduction', payslip.pfDeduction, false)}
                  {row('LOP Deduction', payslip.lopDeduction, false)}
                  {row('Other Deductions', payslip.otherDeductions, false)}
                </div>
                <div className="flex justify-between items-center py-4 mt-2 border-t-2 border-gray-100">
                  <span className="text-base font-bold text-gray-900">Total Deductions</span>
                  <span className="text-lg font-bold text-rose-600">−₹{payslip.totalDeductions.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                </div>
              </div>
            </div>

            {/* Net Salary Footer */}
            <div className="mt-8 bg-gradient-to-r from-emerald-50 to-teal-50 rounded-xl p-6 border border-emerald-100 flex justify-between items-center shadow-sm">
              <div>
                <span className="block text-sm font-bold text-emerald-800 uppercase tracking-wider mb-1">Net Payable Salary</span>
                <span className="text-xs text-emerald-600 font-medium">Amount transferred to bank account</span>
              </div>
              <span className="text-4xl font-black text-emerald-700 tracking-tight">₹{payslip.netSalary.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}