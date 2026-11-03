package com.bank.loan.reports;

import java.math.BigDecimal;

public record DisbursedLoanItem(
        long loanId,
        String customerNationalId,
        String customerName,
        BigDecimal principal,
        BigDecimal interestRate,
        Integer termMonths,
        java.time.LocalDate disbursedAt,
        String approvedBy
) {
}
