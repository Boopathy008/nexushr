import api from './axios';
import type { Employee, PagedResponse, Department, Designation } from '../types/employee';

export const employeeApi = {
  list: (params: {
    search?: string; departmentId?: string; designationId?: string;
    status?: string; page?: number; size?: number; sortBy?: string; sortDir?: string;
  }) => api.get<PagedResponse<Employee>>('/employees', { params }),

  getById: (id: string) => api.get<Employee>(`/employees/${id}`),
  getByUserId: (userId: string) => api.get<Employee>(`/employees/by-user/${userId}`),
  create: (data: unknown) => api.post<Employee>('/employees', data),
  update: (id: string, data: unknown) => api.put<Employee>(`/employees/${id}`, data),
  delete: (id: string) => api.delete(`/employees/${id}`),
};

export const departmentApi = {
  list: () => api.get<Department[]>('/departments'),
  getById: (id: string) => api.get<Department>(`/departments/${id}`),
  create: (data: unknown) => api.post<Department>('/departments', data),
  update: (id: string, data: unknown) => api.put<Department>(`/departments/${id}`, data),
};

export const designationApi = {
  list: () => api.get<Designation[]>('/designations'),
  byDepartment: (departmentId: string) =>
    api.get<Designation[]>(`/designations/department/${departmentId}`),
  create: (data: unknown) => api.post<Designation>('/designations', data),
  update: (id: string, data: unknown) => api.put<Designation>(`/designations/${id}`, data),
};