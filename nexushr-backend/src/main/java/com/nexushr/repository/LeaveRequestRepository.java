package com.nexushr.repository;

import com.nexushr.domain.entity.LeaveRequest;
import com.nexushr.domain.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    Page<LeaveRequest> findAllByEmployeeIdOrderByAppliedAtDesc(
            UUID employeeId, Pageable pageable);

    List<LeaveRequest> findAllByEmployeeIdAndStatus(UUID employeeId, LeaveStatus status);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        JOIN FETCH lr.employee e
        JOIN FETCH lr.leaveType lt
        WHERE lr.id = :id
        """)
    java.util.Optional<LeaveRequest> findByIdWithDetails(@Param("id") UUID id);

    Page<LeaveRequest> findAllByStatusOrderByAppliedAtDesc(
            LeaveStatus status, Pageable pageable);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        JOIN FETCH lr.employee e
        JOIN FETCH lr.leaveType lt
        WHERE e.department.id = :departmentId
        AND lr.status = :status
        ORDER BY lr.appliedAt DESC
        """)
    List<LeaveRequest> findByDepartmentAndStatus(
            @Param("departmentId") UUID departmentId,
            @Param("status") LeaveStatus status);

    long countByStatus(LeaveStatus status);

    @Query("""
    SELECT COUNT(lr) FROM LeaveRequest lr
    WHERE lr.status = :status
    AND lr.appliedAt >= :from
    AND lr.appliedAt <= :to
    """)
    long countByStatusAndDateRange(
            @Param("status") LeaveStatus status,
            @Param("from")   LocalDateTime from,
            @Param("to")     LocalDateTime to);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
        AND lr.status IN ('PENDING','APPROVED')
        AND NOT (lr.endDate < :start OR lr.startDate > :end)
        """)
    List<LeaveRequest> findOverlappingLeaves(
            @Param("employeeId") UUID employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    long countByEmployeeIdAndStatus(UUID employeeId, LeaveStatus status);
}
