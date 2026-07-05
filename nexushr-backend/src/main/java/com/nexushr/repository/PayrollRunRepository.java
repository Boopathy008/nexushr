package com.nexushr.repository;

import com.nexushr.domain.entity.PayrollRun;
import com.nexushr.domain.enums.PayrollStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {

    Optional<PayrollRun> findByEmployeeIdAndPayMonthAndPayYear(
            UUID employeeId, int month, int year);

    boolean existsByEmployeeIdAndPayMonthAndPayYear(
            UUID employeeId, int month, int year);

    List<PayrollRun> findAllByPayMonthAndPayYearOrderByEmployee_FirstName(
            int month, int year);

    List<PayrollRun> findAllByEmployeeIdOrderByPayYearDescPayMonthDesc(UUID employeeId);

    long countByPayMonthAndPayYearAndStatus(
            int month, int year, PayrollStatus status);

    @Query("""
    SELECT COALESCE(SUM(p.netSalary), 0) FROM PayrollRun p
    WHERE p.employee.department.id = :deptId
    AND p.payMonth = :month AND p.payYear = :year
    """)
    BigDecimal sumNetSalaryByDepartmentAndPeriod(
            @Param("deptId") UUID deptId,
            @Param("month")  int month,
            @Param("year")   int year);

    @Query("""
    SELECT COUNT(p) FROM PayrollRun p
    WHERE p.employee.department.id = :deptId
    AND p.payMonth = :month AND p.payYear = :year
    """)
    long countByDepartmentAndPeriod(
            @Param("deptId") UUID deptId,
            @Param("month")  int month,
            @Param("year")   int year);

    @Query("""
        SELECT COALESCE(SUM(p.netSalary), 0) FROM PayrollRun p
        WHERE p.payMonth = :month AND p.payYear = :year
        AND p.status = 'PROCESSED'
        """)
    BigDecimal sumNetSalaryForPeriod(@Param("month") int month, @Param("year") int year);
}
