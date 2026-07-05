import api from './axios';
import type { WorkforceIntelligenceResponse, DepartmentIntelligenceResponse } from '../types/intelligence';

export const intelligenceApi = {
  getEmployeeReport: (employeeId: string) =>
    api.get<WorkforceIntelligenceResponse>(`/intelligence/employee/${employeeId}`),
  getDepartmentReport: (departmentId: string) =>
    api.get<DepartmentIntelligenceResponse>(`/intelligence/department/${departmentId}`),
  getHighRisk: () =>
    api.get<WorkforceIntelligenceResponse[]>('/intelligence/high-risk'),
};