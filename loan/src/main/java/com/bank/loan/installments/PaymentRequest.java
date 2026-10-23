package com.bank.loan.installments;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull
        @Positive
        @Digits(integer = 5, fraction = 2)
        BigDecimal amount
) {
}
