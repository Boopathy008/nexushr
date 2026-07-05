import api from './axios';
import type { PayslipResponse, SalaryStructureRequest } from '../types/payroll';

export const payrollApi = {
  setSalaryStructure: (employeeId: string, data: SalaryStructureRequest) =>
    api.post<PayslipResponse>(`/payroll/${employeeId}/salary-structure`, data),
  generate: (employeeId: string, month: number, year: number) =>
    api.post<PayslipResponse>(`/payroll/${employeeId}/generate`, null, { params: { month, year } }),
  bulkGenerate: (month: number, year: number) =>
    api.post<PayslipResponse[]>('/payroll/bulk-generate', null, { params: { month, year } }),
  getPayslip: (employeeId: string, month: number, year: number) =>
    api.get<PayslipResponse>(`/payroll/${employeeId}/payslip`, { params: { month, year } }),
  getHistory: (employeeId: string) =>
    api.get<PayslipResponse[]>(`/payroll/${employeeId}/history`),
};