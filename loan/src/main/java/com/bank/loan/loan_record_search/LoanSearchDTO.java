package com.bank.loan.loan_record_search;

import com.bank.loan.loan_record.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanSearchDTO(
        BigDecimal amount,
        Integer term,
        Status status,
        LocalDate createdAt
) {
}
