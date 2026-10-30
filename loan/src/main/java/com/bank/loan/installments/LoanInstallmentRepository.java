package com.bank.loan.installments;

import com.bank.loan.loan_record.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long>{
    List<LoanInstallment> findByStatusAndDueDateBefore(InstallmentStatus status, LocalDate date);

    @Query("""
            select installment from LoanInstallment installment
            join fetch installment.loanRecord loan
            join fetch loan.customer customer
            where installment.dueDate < :date
              and installment.status in :installmentStatuses
              and loan.status in :loanStatuses
            order by installment.dueDate, loan.id, installment.installmentNumber
            """)
    List<LoanInstallment> findOverdueInstallments(
            @Param("date") LocalDate date,
            @Param("installmentStatuses") Collection<InstallmentStatus> installmentStatuses,
            @Param("loanStatuses") Collection<Status> loanStatuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select installment from LoanInstallment installment
            join fetch installment.loanRecord loan
            where loan.customer.id = :customerId
              and loan.status in :loanStatuses
              and installment.status in :installmentStatuses
            order by loan.createdAt, loan.id, installment.installmentNumber
            """)
    List<LoanInstallment> findOutstandingForCustomer(
            @Param("customerId") Long customerId,
            @Param("loanStatuses") Collection<Status> loanStatuses,
            @Param("installmentStatuses") Collection<InstallmentStatus> installmentStatuses
    );

    long countByLoanRecordIdAndStatusNot(long loanRecordId, InstallmentStatus status);
}
