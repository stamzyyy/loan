package com.bank.loan.user;


import com.bank.loan.exceptions.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;


    public CreateUserResponse create(CreateUserRequest req) {
        if (repository.existsByUsername(req.username()))
            throw new DuplicateResourceException("Username already taken: " + req.username());
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(req.password());
        user.setRole(req.role());

        User saved = repository.save(user);


        return new CreateUserResponse(saved.getId(), saved.getUsername(), saved.getRole(), saved.isEnabled());


    }
}
