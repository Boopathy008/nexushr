export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface LeaveResponse {
  id: string;
  employeeId: string;
  employeeName: string;
  employeeCode: string;
  leaveTypeName: string;
  leaveTypeCode: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  status: LeaveStatus;
  reason: string;
  rejectionNote: string | null;
  approvedByName: string | null;
  appliedAt: string;
  decidedAt: string | null;
}

export interface LeaveBalanceEntry {
  leaveTypeName: string;
  leaveTypeCode: string;
  isPaid: boolean;
  totalDays: number;
  usedDays: number;
  pendingDays: number;
  availableDays: number;
}

export interface LeaveBalanceResponse {
  year: number;
  employeeName: string;
  balances: LeaveBalanceEntry[];
}

export interface LeaveType {
  id: string;
  name: string;
  code: string;
  annualQuota: number;
  isPaid: boolean;
}