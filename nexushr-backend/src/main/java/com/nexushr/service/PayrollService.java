package com.nexushr.service;

import com.nexushr.domain.entity.*;
import com.nexushr.domain.enums.AttendanceStatus;
import com.nexushr.domain.enums.PayrollStatus;
import com.nexushr.dto.request.payroll.SalaryStructureRequest;
import com.nexushr.dto.response.payroll.PayslipResponse;
import com.nexushr.exception.*;
import com.nexushr.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final PayrollRunRepository      payrollRunRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository        employeeRepository;
    private final AttendanceRepository      attendanceRepository;
    private final LeaveRequestRepository    leaveRequestRepository;
    private final NotificationService       notificationService;

    // ─── Salary structure ─────────────────────────────────────────────────────

    @Transactional
    public PayslipResponse setSalaryStructure(UUID employeeId,
                                              SalaryStructureRequest request) {
        Employee employee = getEmployee(employeeId);
        SalaryStructure structure = salaryStructureRepository
                .findByEmployeeId(employeeId)
                .orElse(SalaryStructure.builder().employee(employee).build());

        structure.setBasicSalary(request.getBasicSalary());
        structure.setHra(request.getHra());
        structure.setTransportAllowance(request.getTransportAllowance());
        structure.setMedicalAllowance(request.getMedicalAllowance());
        structure.setOtherAllowances(request.getOtherAllowances());
        structure.setTaxRate(request.getTaxRate());
        structure.setPfRate(request.getPfRate());
        structure.setEffectiveFrom(request.getEffectiveFrom());
        salaryStructureRepository.save(structure);

        log.info("Salary structure set for employee: {}", employeeId);
        return generatePayroll(employeeId,
                LocalDate.now().getMonthValue(),
                LocalDate.now().getYear());
    }

    // ─── Generate payroll ─────────────────────────────────────────────────────

    @Transactional
    public PayslipResponse generatePayroll(UUID employeeId, int month, int year) {
        Employee employee = getEmployee(employeeId);
        SalaryStructure structure = salaryStructureRepository
                .findByEmployeeId(employeeId)
                .orElseThrow(() -> new BusinessRuleException(
                        "No salary structure found. Please configure salary first."));

        if (payrollRunRepository.existsByEmployeeIdAndPayMonthAndPayYear(
                employeeId, month, year)) {
            return getPayslip(employeeId, month, year);
        }

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

        int workingDays = countWorkingDays(from, to);
        AttendanceSummary summary = computeAttendanceSummary(
                employeeId, from, to, workingDays);

        BigDecimal grossSalary = structure.getGrossSalary();
        BigDecimal lopDeduction = computeLopDeduction(
                grossSalary, summary.lopDays(), workingDays);
        BigDecimal grossAfterLop = grossSalary.subtract(lopDeduction);

        BigDecimal taxDeduction = grossAfterLop
                .multiply(structure.getTaxRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal pfDeduction  = structure.getBasicSalary()
                .multiply(structure.getPfRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal totalDeductions = taxDeduction
                .add(pfDeduction).add(lopDeduction);
        BigDecimal netSalary = grossAfterLop.subtract(taxDeduction).subtract(pfDeduction);

        BigDecimal allowances = structure.getTransportAllowance()
                .add(structure.getMedicalAllowance())
                .add(structure.getOtherAllowances());

        PayrollRun run = PayrollRun.builder()
                .employee(employee)
                .payMonth(month)
                .payYear(year)
                .workingDays(workingDays)
                .presentDays(summary.presentDays())
                .leaveDays(summary.leaveDays())
                .lopDays(summary.lopDays())
                .basicSalary(structure.getBasicSalary())
                .hra(structure.getHra())
                .allowances(allowances)
                .grossSalary(grossSalary)
                .taxDeduction(taxDeduction)
                .pfDeduction(pfDeduction)
                .lopDeduction(lopDeduction)
                .otherDeductions(BigDecimal.ZERO)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .status(PayrollStatus.PROCESSED)
                .processedAt(LocalDateTime.now())
                .build();

        payrollRunRepository.save(run);
        notificationService.sendPayslipNotification(run);
        log.info("Payroll generated: employee={} period={}/{} net={}",
                employeeId, month, year, netSalary);
        return toPayslip(run);
    }

    @Transactional
    public List<PayslipResponse> generateBulkPayroll(int month, int year) {
        List<Employee> activeEmployees = employeeRepository
                .findAllWithFilters(
                        com.nexushr.domain.enums.EmployeeStatus.ACTIVE, null,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        return activeEmployees.stream()
                .filter(e -> salaryStructureRepository.existsByEmployeeId(e.getId()))
                .map(e -> generatePayroll(e.getId(), month, year))
                .toList();
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PayslipResponse getPayslip(UUID employeeId, int month, int year) {
        return payrollRunRepository
                .findByEmployeeIdAndPayMonthAndPayYear(employeeId, month, year)
                .map(this::toPayslip)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payroll not found for " + month + "/" + year));
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> getPayrollHistory(UUID employeeId) {
        return payrollRunRepository
                .findAllByEmployeeIdOrderByPayYearDescPayMonthDesc(employeeId)
                .stream().map(this::toPayslip).toList();
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> getPayrollByPeriod(int month, int year) {
        return payrollRunRepository
                .findAllByPayMonthAndPayYearOrderByEmployee_FirstName(month, year)
                .stream().map(this::toPayslip).toList();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private record AttendanceSummary(BigDecimal presentDays,
                                     BigDecimal leaveDays,
                                     BigDecimal lopDays) {}

    private AttendanceSummary computeAttendanceSummary(UUID employeeId,
                                                       LocalDate from,
                                                       LocalDate to,
                                                       int workingDays) {
        List<Attendance> records = attendanceRepository
                .findAllByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        employeeId, from, to);

        long present = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long half    = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
        long onLeave = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_LEAVE).count();

        BigDecimal presentDays = BigDecimal.valueOf(present)
                .add(BigDecimal.valueOf(half).multiply(new BigDecimal("0.5")));
        BigDecimal leaveDays   = BigDecimal.valueOf(onLeave);
        BigDecimal accounted   = presentDays.add(leaveDays);
        BigDecimal lopDays     = BigDecimal.valueOf(workingDays)
                .subtract(accounted).max(BigDecimal.ZERO);

        return new AttendanceSummary(presentDays, leaveDays, lopDays);
    }

    private BigDecimal computeLopDeduction(BigDecimal grossSalary,
                                           BigDecimal lopDays, int workingDays) {
        if (lopDays.compareTo(BigDecimal.ZERO) == 0 || workingDays == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal perDayRate = grossSalary.divide(
                BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP);
        return perDayRate.multiply(lopDays).setScale(2, RoundingMode.HALF_UP);
    }

    private int countWorkingDays(LocalDate from, LocalDate to) {
        return (int) from.datesUntil(to.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                        && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    private Employee getEmployee(UUID employeeId) {
        return employeeRepository.findByIdWithDetails(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));
    }

    public PayslipResponse toPayslip(PayrollRun r) {
        String period = Month.of(r.getPayMonth())
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + r.getPayYear();
        return PayslipResponse.builder()
                .id(r.getId())
                .employeeName(r.getEmployee().getFullName())
                .employeeCode(r.getEmployee().getEmployeeCode())
                .department(r.getEmployee().getDepartment().getName())
                .designation(r.getEmployee().getDesignation().getTitle())
                .payMonth(r.getPayMonth()).payYear(r.getPayYear())
                .payPeriod(period)
                .workingDays(r.getWorkingDays())
                .presentDays(r.getPresentDays())
                .leaveDays(r.getLeaveDays())
                .lopDays(r.getLopDays())
                .basicSalary(r.getBasicSalary())
                .hra(r.getHra())
                .allowances(r.getAllowances())
                .grossSalary(r.getGrossSalary())
                .taxDeduction(r.getTaxDeduction())
                .pfDeduction(r.getPfDeduction())
                .lopDeduction(r.getLopDeduction())
                .otherDeductions(r.getOtherDeductions())
                .totalDeductions(r.getTotalDeductions())
                .netSalary(r.getNetSalary())
                .status(r.getStatus())
                .processedAt(r.getProcessedAt())
                .build();
    }
}