package com.nexushr.repository;

import com.nexushr.domain.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, UUID> {

    List<PerformanceReview> findAllByEmployeeIdOrderByReviewYearDescReviewQuarterDesc(
            UUID employeeId);

    @Query("""
        SELECT COALESCE(AVG(r.rating), 0) FROM PerformanceReview r
        WHERE r.employee.id = :id
        """)
    BigDecimal averageRatingByEmployee(@Param("id") UUID id);

    int countByEmployeeId(UUID employeeId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM PerformanceReview r")
    BigDecimal averageRatingAllEmployees();

    @Query("""
    SELECT COALESCE(AVG(r.rating), 0) FROM PerformanceReview r
    WHERE r.employee.department.id = :deptId
    """)
    BigDecimal averageRatingByDepartment(@Param("deptId") UUID deptId);

    long countByReviewYearAndReviewQuarter(int reviewYear, int reviewQuarter);

    @Query("""
    SELECT COUNT(r) FROM PerformanceReview r
    WHERE r.employee.department.id = :deptId
    AND r.reviewYear = :year
    AND r.reviewQuarter = :quarter
    """)
    long countByDepartmentAndQuarter(
            @Param("deptId")  UUID deptId,
            @Param("year")    int year,
            @Param("quarter") int quarter);

    @Query("""
        SELECT COUNT(r) > 0 FROM PerformanceReview r
        WHERE r.employee.id  = :employeeId
        AND   r.reviewer.id  = :reviewerId
        AND   r.reviewYear   = :year
        AND   r.reviewQuarter = :quarter
        """)
    boolean existsByEmployeeIdAndReviewerIdAndPeriod(
            @Param("employeeId") UUID employeeId,
            @Param("reviewerId") UUID reviewerId,
            @Param("year")    int year,
            @Param("quarter") int quarter);
}
