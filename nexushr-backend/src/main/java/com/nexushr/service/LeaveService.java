package com.nexushr.service;

import com.nexushr.domain.entity.*;
import com.nexushr.domain.enums.AttendanceStatus;
import com.nexushr.domain.enums.LeaveStatus;
import com.nexushr.dto.request.leave.*;
import com.nexushr.dto.response.leave.*;
import com.nexushr.exception.*;
import com.nexushr.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveRequestRepository  leaveRequestRepository;
    private final LeaveBalanceRepository  leaveBalanceRepository;
    private final LeaveTypeRepository     leaveTypeRepository;
    private final EmployeeRepository      employeeRepository;
    private final UserRepository          userRepository;
    private final AttendanceRepository    attendanceRepository;
    private final NotificationService     notificationService;

    // ─── Apply ────────────────────────────────────────────────────────────────

    @Transactional
    public LeaveResponse applyLeave(UUID employeeId, ApplyLeaveRequest request) {
        Employee employee = getEmployee(employeeId);
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        validateDateRange(request.getStartDate(), request.getEndDate());
        checkOverlappingLeaves(employeeId, request.getStartDate(), request.getEndDate());

        BigDecimal days = calculateWorkingDays(request.getStartDate(), request.getEndDate());

        if (leaveType.getAnnualQuota() > 0) {
            LeaveBalance balance = getOrCreateBalance(employee, leaveType,
                    request.getStartDate().getYear());
            if (balance.getAvailableDays().compareTo(days) < 0) {
                throw new BusinessRuleException(
                        String.format("Insufficient %s balance. Available: %.1f, Requested: %.1f",
                                leaveType.getName(),
                                balance.getAvailableDays(), days));
            }
            balance.setPendingDays(balance.getPendingDays().add(days));
            leaveBalanceRepository.save(balance);
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(days)
                .status(LeaveStatus.PENDING)
                .reason(request.getReason())
                .appliedAt(LocalDateTime.now())
                .build();

        leaveRequestRepository.save(leaveRequest);
        notificationService.sendLeaveApplicationNotification(leaveRequest);
        log.info("Leave applied: employee={} type={} days={}", employeeId,
                leaveType.getCode(), days);
        return toResponse(leaveRequest);
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    @Transactional
    public LeaveResponse cancelLeave(UUID leaveRequestId, UUID employeeId) {
        LeaveRequest request = leaveRequestRepository.findByIdWithDetails(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found: " + leaveRequestId));

        if (!request.getEmployee().getId().equals(employeeId)) {
            throw new UnauthorizedAccessException("You can only cancel your own leave requests");
        }
        if (request.getStatus() == LeaveStatus.APPROVED
                && request.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Cannot cancel a leave that has already started");
        }
        if (request.getStatus() == LeaveStatus.REJECTED
                || request.getStatus() == LeaveStatus.CANCELLED) {
            throw new BusinessRuleException("Leave request is already " + request.getStatus());
        }

        releaseBalance(request);
        request.setStatus(LeaveStatus.CANCELLED);
        leaveRequestRepository.save(request);
        return toResponse(request);
    }

    // ─── Approve / Reject ─────────────────────────────────────────────────────

    @Transactional
    public LeaveResponse decideLeave(UUID leaveRequestId, UUID managerId,
                                     LeaveDecisionRequest decision) {
        LeaveRequest request = leaveRequestRepository.findByIdWithDetails(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found: " + leaveRequestId));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessRuleException(
                    "Only pending leave requests can be approved or rejected");
        }
        if (decision.getDecision() == LeaveStatus.REJECTED
                && (decision.getRejectionNote() == null
                || decision.getRejectionNote().isBlank())) {
            throw new BusinessRuleException("Rejection note is required when rejecting a leave");
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        request.setApprovedBy(manager);
        request.setDecidedAt(LocalDateTime.now());
        request.setStatus(decision.getDecision());

        if (decision.getDecision() == LeaveStatus.APPROVED) {
            finalizeApprovedLeave(request);
        } else {
            request.setRejectionNote(decision.getRejectionNote());
            releaseBalance(request);
        }

        leaveRequestRepository.save(request);
        notificationService.sendLeaveDecisionNotification(request);
        return toResponse(request);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LeaveResponse> getMyLeaveHistory(UUID employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leaveRequestRepository
                .findAllByEmployeeIdOrderByAppliedAtDesc(employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LeaveResponse> getPendingLeaves(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leaveRequestRepository
                .findAllByStatusOrderByAppliedAtDesc(LeaveStatus.PENDING, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getDepartmentPendingLeaves(UUID departmentId) {
        return leaveRequestRepository
                .findByDepartmentAndStatus(departmentId, LeaveStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LeaveBalanceResponse getLeaveBalance(UUID employeeId, int year) {
        Employee employee = getEmployee(employeeId);
        List<LeaveType> activeTypes = leaveTypeRepository.findAllByActiveTrue();

        List<LeaveBalanceResponse.BalanceEntry> entries = activeTypes.stream()
                .map(lt -> {
                    LeaveBalance bal = getOrCreateBalance(employee, lt, year);
                    return LeaveBalanceResponse.BalanceEntry.builder()
                            .leaveTypeName(lt.getName())
                            .leaveTypeCode(lt.getCode())
                            .isPaid(lt.isPaid())
                            .totalDays(bal.getTotalDays())
                            .usedDays(bal.getUsedDays())
                            .pendingDays(bal.getPendingDays())
                            .availableDays(bal.getAvailableDays())
                            .build();
                }).toList();

        return LeaveBalanceResponse.builder()
                .year(year)
                .employeeName(employee.getFullName())
                .balances(entries)
                .build();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void finalizeApprovedLeave(LeaveRequest request) {
        LeaveBalance balance = getOrCreateBalance(
                request.getEmployee(),
                request.getLeaveType(),
                request.getStartDate().getYear());

        balance.setPendingDays(
                balance.getPendingDays().subtract(request.getTotalDays()));
        balance.setUsedDays(
                balance.getUsedDays().add(request.getTotalDays()));
        leaveBalanceRepository.save(balance);

        markAttendanceAsLeave(request);
    }

    private void markAttendanceAsLeave(LeaveRequest request) {
        request.getStartDate().datesUntil(request.getEndDate().plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .forEach(date -> {
                    Attendance attendance = attendanceRepository
                            .findByEmployeeIdAndAttendanceDate(
                                    request.getEmployee().getId(), date)
                            .orElse(Attendance.builder()
                                    .employee(request.getEmployee())
                                    .attendanceDate(date)
                                    .workingMinutes(0)
                                    .build());
                    attendance.setStatus(AttendanceStatus.ON_LEAVE);
                    attendanceRepository.save(attendance);
                });
    }

    private void releaseBalance(LeaveRequest request) {
        if (request.getLeaveType().getAnnualQuota() > 0
                && request.getStatus() == LeaveStatus.PENDING) {
            LeaveBalance balance = getOrCreateBalance(
                    request.getEmployee(),
                    request.getLeaveType(),
                    request.getStartDate().getYear());
            balance.setPendingDays(
                    balance.getPendingDays().subtract(request.getTotalDays()));
            leaveBalanceRepository.save(balance);
        }
    }

    private LeaveBalance getOrCreateBalance(Employee employee,
                                            LeaveType leaveType, int year) {
        return leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        employee.getId(), leaveType.getId(), year)
                .orElseGet(() -> {
                    LeaveBalance lb = LeaveBalance.builder()
                            .employee(employee)
                            .leaveType(leaveType)
                            .year(year)
                            .totalDays(BigDecimal.valueOf(leaveType.getAnnualQuota()))
                            .usedDays(BigDecimal.ZERO)
                            .pendingDays(BigDecimal.ZERO)
                            .build();
                    return leaveBalanceRepository.save(lb);
                });
    }

    private BigDecimal calculateWorkingDays(LocalDate start, LocalDate end) {
        long days = start.datesUntil(end.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
        return BigDecimal.valueOf(days);
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new BusinessRuleException("End date cannot be before start date");
        }
        if (calculateWorkingDays(start, end).compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessRuleException(
                    "Leave duration must include at least one working day");
        }
    }

    private void checkOverlappingLeaves(UUID employeeId,
                                        LocalDate start, LocalDate end) {
        List<LeaveRequest> overlapping = leaveRequestRepository
                .findOverlappingLeaves(employeeId, start, end);
        if (!overlapping.isEmpty()) {
            throw new BusinessRuleException(
                    "A leave request already exists for the selected date range");
        }
    }

    private Employee getEmployee(UUID employeeId) {
        return employeeRepository.findByIdWithDetails(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));
    }

    public LeaveResponse toResponse(LeaveRequest lr) {
        return LeaveResponse.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployee().getId())
                .employeeName(lr.getEmployee().getFullName())
                .employeeCode(lr.getEmployee().getEmployeeCode())
                .leaveTypeName(lr.getLeaveType().getName())
                .leaveTypeCode(lr.getLeaveType().getCode())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .totalDays(lr.getTotalDays())
                .status(lr.getStatus())
                .reason(lr.getReason())
                .rejectionNote(lr.getRejectionNote())
                .approvedByName(lr.getApprovedBy() != null
                        ? lr.getApprovedBy().getUsername() : null)
                .appliedAt(lr.getAppliedAt())
                .decidedAt(lr.getDecidedAt())
                .build();
    }
}
