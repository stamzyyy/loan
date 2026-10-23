package com.bank.loan.user;

//import com.bank.loan.user.User.Role;

public record CreateUserResponse(
        long id, String Username, Role role, boolean enabled
) {
}