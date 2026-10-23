package com.bank.loan.loan_record_search;

import com.bank.loan.loan_record.LoanRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
class LoanSearchService {
    private final LoanSearchRepository repository;

    public LoanSearchService(LoanSearchRepository repository) {
        this.repository = repository;
    }

    public Page<LoanSearchDTO> executeSearch(LoanSearchDTO criteria, Pageable pageable) {
        Specification<LoanRecord> spec = LoanSearchSpecification.withCriteria(criteria);

        return repository.findAll(spec, pageable)
                .map(loan -> new LoanSearchDTO( loan.getAmount(), loan.getTerm(),  loan.getStatus(), loan.getCreatedAt()));
    }
}

//String createdBy,
//String approvedBy,
//BigDecimal amount,
//Integer term,
//Status status,
//Instant createdAt
/// /){