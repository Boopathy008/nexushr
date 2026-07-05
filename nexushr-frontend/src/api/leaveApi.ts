import api from './axios';
import type { LeaveResponse, LeaveBalanceResponse } from '../types/leave';
import type { PagedResponse } from '../types/employee';

export const leaveApi = {
  apply: (employeeId: string, data: unknown) =>
    api.post<LeaveResponse>(`/leaves/${employeeId}/apply`, data),
  cancel: (leaveRequestId: string, employeeId: string) =>
    api.patch<LeaveResponse>(`/leaves/${leaveRequestId}/cancel`, null, { params: { employeeId } }),
  decide: (leaveRequestId: string, managerId: string, data: unknown) =>
    api.patch<LeaveResponse>(`/leaves/${leaveRequestId}/decide`, data, { params: { managerId } }),
  getHistory: (employeeId: string, page = 0, size = 20) =>
    api.get<PagedResponse<LeaveResponse>>(`/leaves/${employeeId}/history`, { params: { page, size } }),
  getPending: (page = 0, size = 20) =>
    api.get<PagedResponse<LeaveResponse>>('/leaves/pending', { params: { page, size } }),
  getBalance: (employeeId: string, year?: number) =>
    api.get<LeaveBalanceResponse>(`/leaves/${employeeId}/balance`, { params: { year } }),
};