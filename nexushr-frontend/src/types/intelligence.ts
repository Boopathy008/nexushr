export interface WorkforceIntelligenceResponse {
  employeeId: string;
  employeeName: string;
  employeeCode: string;
  department: string;
  designation: string;

  attritionRiskScore: number;
  attritionRiskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  attritionRiskFactors: string[];

  engagementScore: number;
  engagementLevel: 'DISENGAGED' | 'MODERATE' | 'ENGAGED' | 'HIGHLY_ENGAGED';
  engagementFactors: string[];

  attendanceRatePercent: number;
  lopDaysLast3Months: number;
  presentDaysLast30: number;

  leaveBalanceUsedPercent: number;
  pendingLeaveRequests: number;

  averagePerformanceRating: number;
  totalReviewsCount: number;

  tenureInMonths: number;
  tenureLabel: string;

  recommendations: string[];
}

export interface DepartmentIntelligenceResponse {
  departmentId: string;
  departmentName: string;
  totalEmployees: number;
  avgAttritionRisk: number;
  avgEngagementScore: number;
  avgAttendanceRate: number;
  highRiskCount: number;
  criticalRiskCount: number;
  employeeInsights: WorkforceIntelligenceResponse[];
}