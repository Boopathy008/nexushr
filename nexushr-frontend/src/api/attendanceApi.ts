import api from './axios';
import type { AttendanceResponse, MonthlyAttendanceReport } from '../types/attendance';
import type { PagedResponse } from '../types/employee';

export const attendanceApi = {
  checkIn: (employeeId: string) =>
    api.post<AttendanceResponse>(`/attendance/${employeeId}/check-in`),
  checkOut: (employeeId: string) =>
    api.post<AttendanceResponse>(`/attendance/${employeeId}/check-out`),
  getToday: (employeeId: string) =>
    api.get<AttendanceResponse | null>(`/attendance/${employeeId}/today`),
  getHistory: (employeeId: string, page = 0, size = 30) =>
    api.get<PagedResponse<AttendanceResponse>>(`/attendance/${employeeId}/history`, { params: { page, size } }),
  getMonthlyReport: (employeeId: string, year: number, month: number) =>
    api.get<MonthlyAttendanceReport>(`/attendance/${employeeId}/report/monthly`, { params: { year, month } }),
};