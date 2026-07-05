import api from './axios';
import type { AdminDashboardResponse, EmployeeDashboardResponse } from '../types/dashboard';

export const dashboardApi = {
  getAdmin: () => api.get<AdminDashboardResponse>('/dashboard/admin'),
  getManager: (managerId: string) =>
    api.get(`/dashboard/manager/${managerId}`),
  getEmployee: (employeeId: string) =>
    api.get<EmployeeDashboardResponse>(`/dashboard/employee/${employeeId}`),
};