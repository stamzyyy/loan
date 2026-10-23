package com.bank.loan.installments;

import com.bank.loan.loan_record.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PaymentReceipt(BigDecimal amount, List<Allocation> allocations) {
    public record Allocation(
            Long loanId,
            Integer installmentNumber,
            BigDecimal amountApplied,
            BigDecimal remainingInstallmentAmount,
            InstallmentStatus installmentStatus,
            Status loanStatus,
            LocalDate dueDate
    ) {
    }
}
