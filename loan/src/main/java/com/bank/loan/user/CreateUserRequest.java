package com.bank.loan.user;

//so the difference between a record and a class is that a record is immutable

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50)
        String username,
        @NotBlank @Size(min = 3)
        String password,
        @NotNull
        Role role
) {
}
