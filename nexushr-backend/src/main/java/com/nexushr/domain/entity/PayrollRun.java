package com.nexushr.domain.entity;

import com.nexushr.domain.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payroll_runs",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"employee_id", "pay_month", "pay_year"}))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_month", nullable = false)
    private Integer payMonth;

    @Column(name = "pay_year", nullable = false)
    private Integer payYear;

    @Column(name = "working_days", nullable = false)
    private Integer workingDays;

    @Column(name = "present_days", precision = 5, scale = 1)
    private BigDecimal presentDays = BigDecimal.ZERO;

    @Column(name = "leave_days", precision = 5, scale = 1)
    private BigDecimal leaveDays = BigDecimal.ZERO;

    @Column(name = "lop_days", precision = 5, scale = 1)
    private BigDecimal lopDays = BigDecimal.ZERO;

    @Column(name = "basic_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 12, scale = 2)
    private BigDecimal hra;

    @Column(precision = 12, scale = 2)
    private BigDecimal allowances;

    @Column(name = "gross_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "tax_deduction", precision = 12, scale = 2)
    private BigDecimal taxDeduction = BigDecimal.ZERO;

    @Column(name = "pf_deduction", precision = 12, scale = 2)
    private BigDecimal pfDeduction = BigDecimal.ZERO;

    @Column(name = "lop_deduction", precision = 12, scale = 2)
    private BigDecimal lopDeduction = BigDecimal.ZERO;

    @Column(name = "other_deductions", precision = 12, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "net_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
