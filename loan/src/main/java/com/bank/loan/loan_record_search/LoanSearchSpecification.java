package com.bank.loan.loan_record_search;

import com.bank.loan.loan_record.LoanRecord;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

//
//@JoinColumn(name = "customer_id", nullable = false)
//private Customer customer;
//
//@Column(nullable = false)
//private BigDecimal amount;
//
//@Column(nullable = false)
//private BigDecimal interestRate;
//
//@Column(nullable = false)
//private Integer term;
//
//@Enumerated(EnumType.STRING)
//@Column(nullable = false)
//private Status status = Status.APPLIED;
//
//@ManyToOne(fetch = FetchType.LAZY)
//@JoinColumn(name = "created_by")
//private User createdBy;
//
//@ManyToOne(fetch = FetchType.LAZY)
//@JoinColumn(name = "approved_by")
//private User approvedBy;
//
//@Column(length = 1000)
//private String rejectionReason;
//
//@Column(nullable = false, updatable = false)
//private Instant createdAt = Instant.now();
//
//private Instant approvedAt;
public class LoanSearchSpecification {

    public static Specification<LoanRecord> withCriteria(LoanSearchDTO criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null){
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
            //yaramba so lets use springUtils
//            if (criteria.status() !=null && !criteria.status().isBlank()){
//                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.status()));
//            }
            if (StringUtils.hasText(String.valueOf(criteria.status()))) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.status()));
            }

            if (criteria.amount() != null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), criteria.amount()));
            }

            if (criteria.term() != null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("term"), criteria.term()));
            }

            if (criteria.createdAt() != null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), criteria.createdAt()));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
