package com.nexushr.service;

import com.nexushr.domain.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Async
    public void sendLeaveApplicationNotification(LeaveRequest request) {
        String subject = "Leave Application Received — " + request.getLeaveType().getName();
        String body = String.format("""
                Dear %s,

                Your %s request from %s to %s (%.1f days) has been received
                and is pending approval.

                Reason: %s

                You will be notified once a decision is made.

                — NexusHR
                """,
                request.getEmployee().getFullName(),
                request.getLeaveType().getName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalDays(),
                request.getReason());

        sendMail(request.getEmployee().getUser().getEmail(), subject, body);
    }

    @Async
    public void sendLeaveDecisionNotification(LeaveRequest request) {
        String decision = request.getStatus().name();
        String subject  = "Leave Request " + decision;
        String body = String.format("""
                Dear %s,

                Your %s request from %s to %s has been %s.

                %s

                — NexusHR
                """,
                request.getEmployee().getFullName(),
                request.getLeaveType().getName(),
                request.getStartDate(),
                request.getEndDate(),
                decision,
                request.getRejectionNote() != null
                        ? "Note: " + request.getRejectionNote() : "");

        sendMail(request.getEmployee().getUser().getEmail(), subject, body);
    }

    @Async
    public void sendPayslipNotification(PayrollRun run) {
        String subject = String.format("Payslip for %s/%s is ready",
                run.getPayMonth(), run.getPayYear());
        String body = String.format("""
                Dear %s,

                Your payslip for %s/%s has been processed.

                Gross Salary : ₹%s
                Deductions   : ₹%s
                Net Salary   : ₹%s

                Please login to NexusHR to view your full payslip.

                — NexusHR
                """,
                run.getEmployee().getFullName(),
                run.getPayMonth(), run.getPayYear(),
                run.getGrossSalary(),
                run.getTotalDeductions(),
                run.getNetSalary());

        sendMail(run.getEmployee().getUser().getEmail(), subject, body);
    }

    private void sendMail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to: {} | Subject: {}", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
