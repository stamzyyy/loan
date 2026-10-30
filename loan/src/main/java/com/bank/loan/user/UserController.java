package com.bank.loan.user;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping("api/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateUserResponse> create(
            @Valid @RequestBody CreateUserRequest req
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.create(req));
    }

}
