package com.nexushr.repository;

import com.nexushr.domain.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    List<LeaveBalance> findAllByEmployeeIdAndYear(UUID employeeId, int year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            UUID employeeId, UUID leaveTypeId, int year);

    @Query("""
        SELECT lb FROM LeaveBalance lb
        JOIN FETCH lb.leaveType lt
        WHERE lb.employee.id = :employeeId
        AND lb.year = :year
        AND lt.active = true
        ORDER BY lt.name
        """)
    List<LeaveBalance> findBalancesWithLeaveType(
            @Param("employeeId") UUID employeeId,
            @Param("year") int year);
}