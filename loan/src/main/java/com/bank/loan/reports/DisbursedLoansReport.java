package com.bank.loan.reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DisbursedLoansReport(
        String period,
        LocalDate from,
        LocalDate through,
        long loanCount,
        BigDecimal totalPrincipal,
        List<DisbursedLoanItem> loans
) {
}
