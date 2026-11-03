package com.bank.loan.loan_record;

import com.bank.loan.customer.Customer;
import com.bank.loan.exceptions.BusinessRuleViolationException;
import com.bank.loan.installments.LoanInstallment;
import com.bank.loan.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
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
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @CreatedBy
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false)
    @LastModifiedBy
    private User approvedBy;

    @Column(length = 1000)
    private String rejectionReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    private LocalDate approvedAt;


    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModified;

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

    public static LocalDate[] getStartDateAndEndDate(String period) {
        LocalDate today = LocalDate.now(ZoneId.of("GMT+2"));
        LocalDate startDate;
        LocalDate endDate;
        switch (period) {
            case "today" -> {
                startDate = today;
                endDate = today.plusDays(1);
            }
            case "month" -> {
                startDate = today.withDayOfMonth(1);
                endDate = startDate.plusMonths(1);
            }
            default -> throw new BusinessRuleViolationException(
                    "Report period must be either 'today' or 'month'");
        }

        return new LocalDate[]{startDate, endDate};

    }

    public void approve(User checker) {
        this.status = Status.APPROVED;
        this.approvedBy = checker;
        this.approvedAt = LocalDate.now();
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

    public void addInstallments(LoanRecord loanRecord) {
        BigDecimal totalRepayment = loanRecord.getAmount()
                .add(loanRecord.getAmount()
                        .multiply(loanRecord.getInterestRate())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal regularAmount = totalRepayment
                .divide(BigDecimal.valueOf(loanRecord.getTerm()), 2, RoundingMode.DOWN);
        BigDecimal scheduledTotal = BigDecimal.ZERO;
        LocalDate approvalDate = LocalDate.now(ZoneId.of("GMT+2"));

        for (int installmentNumber = 1; installmentNumber <= loanRecord.getTerm(); installmentNumber++) {
            BigDecimal installmentAmount = installmentNumber == loanRecord.getTerm()
                    ? totalRepayment.subtract(scheduledTotal)
                    : regularAmount;
            loanRecord.addInstallment(new LoanInstallment(
                    loanRecord,
                    installmentNumber,
                    installmentAmount,
                    approvalDate.plusMonths(installmentNumber)
            ));
            scheduledTotal = scheduledTotal.add(installmentAmount);
        }
    }

}
