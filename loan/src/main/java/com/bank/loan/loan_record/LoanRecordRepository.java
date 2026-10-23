package com.bank.loan.loan_record;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Collection;
import java.time.Instant;
import java.util.List;

public interface LoanRecordRepository extends JpaRepository<LoanRecord, Long>, RevisionRepository<LoanRecord, Long, Long> {

    Page<LoanRecord> findByCustomerNationalIdContainingIgnoreCase(String nationalId, Pageable pageable);

    Page<LoanRecord> findByStatus(Status status, Pageable pageable);

    long countByCustomerIdAndStatusIn(Long customerId, Collection<Status> statuses);

    List<LoanRecord> findByApprovedAtGreaterThanEqualAndApprovedAtLessThanOrderByApprovedAtDesc(
            Instant start,
            Instant end
    );
}
