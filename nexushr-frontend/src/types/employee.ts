export type EmployeeStatus = 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE' | 'TERMINATED';

export interface DepartmentSummary {
  id: string;
  name: string;
  code: string;
}

export interface DesignationSummary {
  id: string;
  title: string;
  grade: string;
}

export interface Employee {
  id: string;
  userId: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone: string;
  gender: string;
  dateOfBirth: string;
  dateOfJoining: string;
  address: string;
  profilePictureUrl: string;
  status: EmployeeStatus;
  role: string;
  department: DepartmentSummary;
  designation: DesignationSummary;
  createdAt: string;
  updatedAt: string;
}

export interface Department {
  id: string;
  name: string;
  code: string;
  description: string;
  active: boolean;
  employeeCount: number;
}

export interface Designation {
  id: string;
  title: string;
  grade: string;
  active: boolean;
  departmentId: string;
  departmentName: string;
  departmentCode: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}