package com.bank.loan.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @NotBlank(message = "National ID is required")
        @Size(max = 50, message = "National ID must not exceed 50 characters")
        String nationalId,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(?=.*\\d)[+0-9().\\s-]{7,20}$",
                message = "Phone number must contain 7 to 20 characters and use only digits or common phone symbols"
        )
        String phone,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email
) {
}
