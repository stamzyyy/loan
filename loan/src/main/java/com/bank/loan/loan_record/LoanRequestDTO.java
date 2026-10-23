package com.bank.loan.loan_record;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanRequestDTO(
        @NotBlank
        String nationalId,

        @Positive(message = "Amount is required")
        BigDecimal amount,

        @NotNull(message = "Interest is required")
        @Positive(message = "Interest should be positive")
        @DecimalMin(value = "1.00", inclusive = false, message = "Interest rate must be greater than 1%")
        BigDecimal interestRate,

        @NotNull(message = "Term is required")
        @Min(value = 1, message = "Term must be at least 1 month")
        @Max(value = 36, message = "Term cannot exceed 36 months")
        Integer term
) {
}
