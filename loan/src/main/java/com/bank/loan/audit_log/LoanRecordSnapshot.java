package com.bank.loan.audit_log;

import com.bank.loan.loan_record.Status;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanRecordSnapshot(
        Status status,
        BigDecimal amount,
        BigDecimal interestRate,
        Integer term,
        String rejectionReason,
        Instant createdAt,
        Instant approvedAt,
        Long customerId,
        Long createdById,
        Long approvedById
) {
}
