package com.bank.loan.reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OverdueInstallmentsReport(
        LocalDate asOfDate,
        long installmentCount,
        BigDecimal totalOutstanding,
        List<OverdueInstallmentItem> installments
) {
}
