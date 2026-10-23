package com.bank.loan.loan_record_search;

import com.bank.loan.loan_record.LoanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
interface LoanSearchRepository extends JpaRepository<LoanRecord, Long>, JpaSpecificationExecutor<LoanRecord> {

}
// Leave this completely empty!

