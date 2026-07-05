package com.nexushr.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"employee_id", "leave_type_id", "year"}))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "total_days", precision = 5, scale = 1)
    private BigDecimal totalDays = BigDecimal.ZERO;

    @Column(name = "used_days", precision = 5, scale = 1)
    private BigDecimal usedDays = BigDecimal.ZERO;

    @Column(name = "pending_days", precision = 5, scale = 1)
    private BigDecimal pendingDays = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BigDecimal getAvailableDays() {
        return totalDays.subtract(usedDays).subtract(pendingDays);
    }
}