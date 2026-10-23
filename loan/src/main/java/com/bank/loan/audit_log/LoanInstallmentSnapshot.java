package com.bank.loan.audit_log;

import com.bank.loan.installments.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanInstallmentSnapshot(
        Integer installmentNumber,
        BigDecimal amount,
        BigDecimal amountPaid,
        LocalDate dueDate,
        InstallmentStatus status,
        Long loanRecordId
) {
}
