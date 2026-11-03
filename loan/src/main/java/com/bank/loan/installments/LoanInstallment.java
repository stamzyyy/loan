package com.bank.loan.installments;

import com.bank.loan.loan_record.LoanRecord;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "loan_installments")
public class LoanInstallment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_record_id", nullable = false)
    private LoanRecord loanRecord;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentStatus status = InstallmentStatus.PENDING;

    public LoanInstallment(
            LoanRecord loanRecord,
            Integer installmentNumber,
            BigDecimal amount,
            LocalDate dueDate
    ) {
        this.loanRecord = loanRecord;
        this.installmentNumber = installmentNumber;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    public BigDecimal getRemainingAmount() {
        return amount.subtract(amountPaid);
    }

    public InstallmentStatus getStatus() {
        if (status != InstallmentStatus.PAID
                && dueDate.isBefore(LocalDate.now(ZoneId.of("GMT+2")))) {
            return InstallmentStatus.OVERDUE;
        }
        return status;
    }

    public InstallmentStatus getRecordedStatus() {
        return status;
    }

    public void applyPayment(BigDecimal payment) {
        amountPaid = amountPaid.add(payment);
        if (amountPaid.compareTo(amount) >= 0) {
            amountPaid = amount;
            status = InstallmentStatus.PAID;
        }
    }
}
