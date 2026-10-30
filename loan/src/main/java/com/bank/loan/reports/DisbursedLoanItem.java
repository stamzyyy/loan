package com.bank.loan.reports;

import java.math.BigDecimal;
import java.time.Instant;

public record DisbursedLoanItem(
        long loanId,
        String customerNationalId,
        String customerName,
        BigDecimal principal,
        BigDecimal interestRate,
        Integer termMonths,
        Instant disbursedAt,
        String approvedBy
) {
}
