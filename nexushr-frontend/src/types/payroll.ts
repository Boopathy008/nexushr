export type PayrollStatus = 'DRAFT' | 'PROCESSED' | 'PAID';

export interface PayslipResponse {
  id: string;
  employeeName: string;
  employeeCode: string;
  department: string;
  designation: string;
  payMonth: number;
  payYear: number;
  payPeriod: string;
  workingDays: number;
  presentDays: number;
  leaveDays: number;
  lopDays: number;
  basicSalary: number;
  hra: number;
  allowances: number;
  grossSalary: number;
  taxDeduction: number;
  pfDeduction: number;
  lopDeduction: number;
  otherDeductions: number;
  totalDeductions: number;
  netSalary: number;
  status: PayrollStatus;
  processedAt: string | null;
}

export interface SalaryStructureRequest {
  basicSalary: number;
  hra: number;
  transportAllowance: number;
  medicalAllowance: number;
  otherAllowances: number;
  taxRate: number;
  pfRate: number;
  effectiveFrom: string;
}