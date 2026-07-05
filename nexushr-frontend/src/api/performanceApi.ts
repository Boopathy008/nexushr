import api from './axios';
import type { ReviewResponse, GoalResponse, ReviewRequest, GoalRequest } from '../types/performance';

export const performanceApi = {
  submitReview: (reviewerId: string, data: ReviewRequest) =>
    api.post<ReviewResponse>('/performance/reviews', data, { params: { reviewerId } }),
  getReviews: (employeeId: string) =>
    api.get<ReviewResponse[]>(`/performance/${employeeId}/reviews`),
  getAverageRating: (employeeId: string) =>
    api.get<number>(`/performance/${employeeId}/rating`),

  createGoal: (data: GoalRequest) =>
    api.post<GoalResponse>('/performance/goals', data),
  getGoals: (employeeId: string) =>
    api.get<GoalResponse[]>(`/performance/${employeeId}/goals`),
  updateGoalStatus: (goalId: string, status: string) =>
    api.patch<GoalResponse>(`/performance/goals/${goalId}/status`, null, { params: { status } }),
};