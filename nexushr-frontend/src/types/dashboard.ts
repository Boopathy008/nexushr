export interface AdminDashboardResponse {
  totalEmployees: number;
  activeEmployees: number;
  inactiveEmployees: number;
  terminatedEmployees: number;
  newJoineesThisMonth: number;
  totalDepartments: number;
  departmentHeadcounts: {
    departmentName: string;
    departmentCode: string;
    activeCount: number;
    totalCount: number;
    headcountPercentage: number;
  }[];
  checkedInToday: number;
  absentToday: number;
  onLeaveToday: number;
  todayAttendancePercentage: number;
  pendingLeaveRequests: number;
  approvedLeavesThisMonth: number;
  rejectedLeavesThisMonth: number;
  totalPayrollThisMonth: number;
  processedPayslipsThisMonth: number;
  pendingPayslipsThisMonth: number;
  averagePerformanceRating: number;
  reviewsSubmittedThisQuarter: number;
  criticalAttritionRiskCount: number;
  highAttritionRiskCount: number;
  mediumAttritionRiskCount: number;
  lowAttritionRiskCount: number;
  disengagedEmployeesCount: number;
}

export interface EmployeeDashboardResponse {
  employeeId: string;
  employeeName: string;
  employeeCode: string;
  designation: string;
  department: string;
  dateOfJoining: string;
  tenureLabel: string;
  profilePictureUrl: string | null;

  checkedInToday: boolean;
  checkInTime: string | null;
  checkOutTime: string | null;
  workingHoursToday: string;
  attendanceStatusToday: string;

  workingDaysThisMonth: number;
  presentDaysThisMonth: number;
  absentDaysThisMonth: number;
  halfDaysThisMonth: number;
  lopDaysThisMonth: number;
  attendancePercentageThisMonth: number;

  leaveBalances: {
    leaveTypeName: string;
    leaveTypeCode: string;
    isPaid: boolean;
    totalDays: number;
    usedDays: number;
    availableDays: number;
    usagePercentage: number;
  }[];
  pendingLeaveRequests: number;
  nextLeave: string | null;

  lastMonthNetSalary: number;
  lastMonthGrossSalary: number;
  lastMonthDeductions: number;
  lastPayslipPeriod: string;
  lastPayslipStatus: string;

  averagePerformanceRating: number;
  ratingLabel: string;
  totalReviewsCount: number;
  latestQuarterRating: number;
  latestReviewPeriod: string;

  goalsInProgress: number;
  goalsCompleted: number;
  goalsTotal: number;
}