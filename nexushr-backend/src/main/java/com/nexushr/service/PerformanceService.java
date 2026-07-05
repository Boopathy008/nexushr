package com.nexushr.service;

import com.nexushr.domain.entity.*;
import com.nexushr.dto.request.performance.GoalRequest;
import com.nexushr.dto.request.performance.ReviewRequest;
import com.nexushr.dto.response.performance.GoalResponse;
import com.nexushr.dto.response.performance.ReviewResponse;
import com.nexushr.exception.*;
import com.nexushr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceReviewRepository reviewRepository;
    private final PerformanceGoalRepository   goalRepository;
    private final EmployeeRepository          employeeRepository;
    private final UserRepository              userRepository;

    // ── Reviews ───────────────────────────────────────────────────────────────

    @Transactional
    public ReviewResponse submitReview(UUID reviewerId, ReviewRequest request) {
        Employee employee = employeeRepository
                .findByIdWithDetails(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + request.getEmployeeId()));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reviewer not found: " + reviewerId));

        boolean exists = reviewRepository
                .existsByEmployeeIdAndReviewerIdAndPeriod(
                        request.getEmployeeId(), reviewerId,
                        request.getReviewYear(), request.getReviewQuarter());
        if (exists) {
            throw new DuplicateResourceException(
                    "A review already exists for Q"
                            + request.getReviewQuarter()
                            + " " + request.getReviewYear());
        }

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .reviewer(reviewer)
                .reviewYear(request.getReviewYear())
                .reviewQuarter(request.getReviewQuarter())
                .rating(request.getRating())
                .feedback(request.getFeedback())
                .strengths(request.getStrengths())
                .improvements(request.getImprovements())
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .build();

        return toReviewResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getEmployeeReviews(UUID employeeId) {
        return reviewRepository
                .findAllByEmployeeIdOrderByReviewYearDescReviewQuarterDesc(employeeId)
                .stream().map(this::toReviewResponse).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageRating(UUID employeeId) {
        BigDecimal avg = reviewRepository.averageRatingByEmployee(employeeId);
        return avg != null
                ? avg.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    // ── Goals ─────────────────────────────────────────────────────────────────

    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        Employee employee = employeeRepository
                .findByIdWithDetails(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + request.getEmployeeId()));

        PerformanceGoal goal = PerformanceGoal.builder()
                .employee(employee)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetDate(request.getTargetDate())
                .status(request.getStatus() != null
                        ? request.getStatus() : "IN_PROGRESS")
                .year(request.getYear())
                .quarter(request.getQuarter())
                .build();

        return toGoalResponse(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getEmployeeGoals(UUID employeeId) {
        return goalRepository
                .findAllByEmployeeIdOrderByYearDescQuarterDesc(employeeId)
                .stream().map(this::toGoalResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getGoalsByQuarter(UUID employeeId,
                                                int year, int quarter) {
        return goalRepository
                .findAllByEmployeeIdAndYearAndQuarter(employeeId, year, quarter)
                .stream().map(this::toGoalResponse).toList();
    }

    @Transactional
    public GoalResponse updateGoalStatus(UUID goalId, String status) {
        PerformanceGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Goal not found: " + goalId));
        goal.setStatus(status);
        return toGoalResponse(goalRepository.save(goal));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private ReviewResponse toReviewResponse(PerformanceReview r) {
        String ratingLabel = resolveRatingLabel(r.getRating());
        String period      = "Q" + r.getReviewQuarter() + " " + r.getReviewYear();

        return ReviewResponse.builder()
                .id(r.getId())
                .employeeId(r.getEmployee().getId())
                .employeeName(r.getEmployee().getFullName())
                .employeeCode(r.getEmployee().getEmployeeCode())
                .reviewerName(r.getReviewer().getUsername())
                .reviewYear(r.getReviewYear())
                .reviewQuarter(r.getReviewQuarter())
                .reviewPeriod(period)
                .rating(r.getRating())
                .ratingLabel(ratingLabel)
                .feedback(r.getFeedback())
                .strengths(r.getStrengths())
                .improvements(r.getImprovements())
                .status(r.getStatus())
                .submittedAt(r.getSubmittedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private GoalResponse toGoalResponse(PerformanceGoal g) {
        String quarterLabel = "Q" + g.getQuarter() + " " + g.getYear();

        return GoalResponse.builder()
                .id(g.getId())
                .employeeId(g.getEmployee().getId())
                .employeeName(g.getEmployee().getFullName())
                .title(g.getTitle())
                .description(g.getDescription())
                .targetDate(g.getTargetDate())
                .status(g.getStatus())
                .year(g.getYear())
                .quarter(g.getQuarter())
                .quarterLabel(quarterLabel)
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }

    private String resolveRatingLabel(BigDecimal rating) {
        if (rating == null) return "Not Rated";
        double r = rating.doubleValue();
        if (r >= 4.5) return "Outstanding";
        if (r >= 4.0) return "Exceeds Expectations";
        if (r >= 3.0) return "Meets Expectations";
        if (r >= 2.0) return "Needs Improvement";
        return "Unsatisfactory";
    }
}