package com.bank.loan.loan_record;

import com.bank.loan.customer.Customer;
import com.bank.loan.installments.LoanInstallment;
import com.bank.loan.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
@Table(name = "loan_records")
public class LoanRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer term;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.APPLIED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant approvedAt;

    @OneToMany(mappedBy = "loanRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("installmentNumber ASC")
    private List<LoanInstallment> installments = new ArrayList<>();

    public LoanRecord(Customer customer, BigDecimal amount, BigDecimal interestRate, Integer term, User createdBy) {
        this.customer = customer;
        this.amount = amount;
        this.interestRate = interestRate;
        this.term = term;
        this.createdBy = createdBy;
        this.status = Status.APPLIED;

    }

    public void approve(User checker) {
        this.status = Status.APPROVED;
        this.approvedBy = checker;
        this.approvedAt = Instant.now();
        this.customer.setHasLoan(true);
    }

    public void addInstallment(LoanInstallment installment) {
        installments.add(installment);
    }

    public void close() {
        this.status = Status.CLOSED;
    }

    public void reject(User checker, String rejectionReason) {
        this.status = Status.REJECTED;
        this.approvedBy = checker;
        this.rejectionReason = rejectionReason;
    }
}
