package com.bank.loan.loan_record_search;

import com.bank.loan.loan_record.LoanRecord;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

public class LoanSearchSpecification {

    public static Specification<LoanRecord> withCriteria(LoanSearchDTO criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null){
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

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
