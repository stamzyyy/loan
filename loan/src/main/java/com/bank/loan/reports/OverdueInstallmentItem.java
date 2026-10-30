package com.bank.loan.reports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueInstallmentItem(
        Long installmentId,
        long loanId,
        String customerNationalId,
        String customerName,
        Integer installmentNumber,
        LocalDate dueDate,
        BigDecimal amount,
        BigDecimal amountPaid,
        BigDecimal outstandingAmount
) {
}
