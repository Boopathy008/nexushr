package com.nexushr.dto.response.leave;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class LeaveBalanceResponse {
    private int year;
    private String employeeName;
    private List<BalanceEntry> balances;

    @Data @Builder
    public static class BalanceEntry {
        private String leaveTypeName;
        private String leaveTypeCode;
        private boolean isPaid;
        private BigDecimal totalDays;
        private BigDecimal usedDays;
        private BigDecimal pendingDays;
        private BigDecimal availableDays;
    }
}
