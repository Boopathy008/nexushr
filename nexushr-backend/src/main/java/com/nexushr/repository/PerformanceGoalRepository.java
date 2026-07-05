package com.nexushr.repository;

import com.nexushr.domain.entity.PerformanceGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PerformanceGoalRepository extends JpaRepository<PerformanceGoal, UUID> {

    List<PerformanceGoal> findAllByEmployeeIdOrderByYearDescQuarterDesc(UUID employeeId);

    List<PerformanceGoal> findAllByEmployeeIdAndYearAndQuarter(
            UUID employeeId, int year, int quarter);
    @Query("""
    SELECT COUNT(g) FROM PerformanceGoal g
    WHERE g.employee.department.id = :deptId
    AND g.status = :status
    """)
    long countByDepartmentIdAndStatus(
            @Param("deptId") UUID deptId,
            @Param("status") String status);

    long countByEmployeeIdAndStatus(UUID employeeId, String status);


}
