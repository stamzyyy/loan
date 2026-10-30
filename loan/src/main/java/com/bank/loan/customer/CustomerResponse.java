package com.bank.loan.customer;

public record CustomerResponse(
        long id,
        String fullName,
        String nationalId,
        String email,
        String phone
) {
}
