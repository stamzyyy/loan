package com.bank.loan.customer;

import com.bank.loan.exceptions.ResourceNotFoundException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String nationalId;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean hasLoan = false;




    private Instant createdAt = Instant.now();

    public boolean getHasLoan() {
        return hasLoan;
    }

}
