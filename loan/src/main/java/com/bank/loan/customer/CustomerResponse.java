package com.bank.loan.customer;

public record CustomerResponse(
        Long id,
        String fullName,
        String nationalId,
        String email,
        String phone
) {
}
