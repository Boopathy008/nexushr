export type AttendanceStatus =
  | 'PRESENT' | 'ABSENT' | 'HALF_DAY' | 'HOLIDAY' | 'WEEKEND' | 'ON_LEAVE';

export interface AttendanceResponse {
  id: string;
  employeeId: string;
  employeeCode: string;
  employeeName: string;
  attendanceDate: string;
  checkInTime: string | null;
  checkOutTime: string | null;
  status: AttendanceStatus;
  workingMinutes: number;
  workingHours: string;
  notes: string | null;
}

export interface MonthlyAttendanceReport {
  employeeName: string;
  employeeCode: string;
  year: number;
  month: number;
  totalWorkingDays: number;
  presentDays: number;
  absentDays: number;
  halfDays: number;
  leaveDays: number;
  holidayDays: number;
  totalWorkingMinutes: number;
  totalWorkingHours: string;
  attendancePercentage: number;
  dailyRecords: AttendanceResponse[];
}