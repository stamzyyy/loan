package com.bank.loan.loan_record_search;
import com.bank.loan.loan_record.Status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

public record LoanSearchDTO (
        BigDecimal amount,
        Integer term,
        Status status,
        Instant createdAt
){
}
