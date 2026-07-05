package com.nexushr.dto.response.attendance;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class MonthlyAttendanceReport {
    private String employeeName;
    private String employeeCode;
    private int year;
    private int month;
    private int totalWorkingDays;
    private int presentDays;
    private int absentDays;
    private int halfDays;
    private int leaveDays;
    private int holidayDays;
    private long totalWorkingMinutes;
    private String totalWorkingHours;
    private double attendancePercentage;
    private List<AttendanceResponse> dailyRecords;
}
