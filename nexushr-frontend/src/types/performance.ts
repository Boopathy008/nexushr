export interface ReviewResponse {
  id: string;
  employeeId: string;
  employeeName: string;
  employeeCode: string;
  reviewerName: string;
  reviewYear: number;
  reviewQuarter: number;
  reviewPeriod: string;
  rating: number;
  ratingLabel: string;
  feedback: string;
  strengths: string | null;
  improvements: string | null;
  status: string;
  submittedAt: string | null;
  createdAt: string;
}

export interface GoalResponse {
  id: string;
  employeeId: string;
  employeeName: string;
  title: string;
  description: string | null;
  targetDate: string | null;
  status: string;
  year: number;
  quarter: number;
  quarterLabel: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewRequest {
  employeeId: string;
  reviewYear: number;
  reviewQuarter: number;
  rating: number;
  feedback: string;
  strengths?: string;
  improvements?: string;
}

export interface GoalRequest {
  employeeId: string;
  title: string;
  description?: string;
  targetDate?: string;
  year: number;
  quarter: number;
  status?: string;
}