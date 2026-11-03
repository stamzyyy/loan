package com.bank.loan.loan_record;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LoanRecordRepository extends JpaRepository<LoanRecord, Long> {

    Page<LoanRecord> findByCustomerNationalIdContainingIgnoreCase(String nationalId, Pageable pageable);

    Page<LoanRecord> findByStatus(Status status, Pageable pageable);

    long countByCustomerIdAndStatusIn(long customerId, Collection<Status> statuses);

    List<LoanRecord> findByApprovedAtGreaterThanEqualAndApprovedAtLessThanOrderByApprovedAtDesc(
            LocalDate start,
            LocalDate end
    );

}