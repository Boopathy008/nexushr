package com.nexushr.repository;

import com.nexushr.domain.entity.Attendance;
import com.nexushr.domain.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(UUID employeeId, LocalDate date);

    boolean existsByEmployeeIdAndAttendanceDate(UUID employeeId, LocalDate date);

    List<Attendance> findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            UUID employeeId, LocalDate from, LocalDate to);

    Page<Attendance> findAllByEmployeeId(UUID employeeId, Pageable pageable);

    @Query("""
        SELECT a FROM Attendance a
        JOIN FETCH a.employee e
        WHERE a.attendanceDate = :date
        ORDER BY e.firstName
        """)
    List<Attendance> findAllByDate(@Param("date") LocalDate date);

    @Query("""
    SELECT COUNT(a) FROM Attendance a
    WHERE a.attendanceDate = :date
    AND a.status = :status
    """)
    long countByDateAndStatus(
            @Param("date")   LocalDate date,
            @Param("status") AttendanceStatus status);

    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.employee.id = :employeeId
        AND a.attendanceDate BETWEEN :from AND :to
        AND a.status = :status
        """)
    long countByEmployeeAndDateRangeAndStatus(
            @Param("employeeId") UUID employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") AttendanceStatus status);

    @Query("""
        SELECT COALESCE(SUM(a.workingMinutes), 0) FROM Attendance a
        WHERE a.employee.id = :employeeId
        AND YEAR(a.attendanceDate)  = :year
        AND MONTH(a.attendanceDate) = :month
        """)
    Long sumWorkingMinutesForMonth(
            @Param("employeeId") UUID employeeId,
            @Param("year") int year,
            @Param("month") int month);
}
