package com.bank.loan.audit_log;

import com.bank.loan.installments.LoanInstallment;
import com.bank.loan.installments.LoanInstallmentRepository;
import com.bank.loan.loan_record.LoanRecord;
import com.bank.loan.loan_record.LoanRecordRepository;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final LoanRecordRepository loanRecordRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

    public AuditService(LoanRecordRepository loanRecordRepository, LoanInstallmentRepository loanInstallmentRepository) {
        this.loanRecordRepository = loanRecordRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    //
    public static <LoanRecordSnapshot> AuditRevisionResponse<LoanRecordSnapshot> toResponse(
            Revision<Long, ?> revision,
            String entityName,
            Long entityId,
            LoanRecordSnapshot snapshot
    ) {
        var metadata = revision.getMetadata();
        return new AuditRevisionResponse<>(
                metadata.getRevisionNumber().orElseThrow(),
                metadata.getRevisionInstant().map(Object::toString).orElse(null),
                metadata.getRevisionType().name(),
                entityName,
                entityId,
                (com.bank.loan.audit_log.LoanRecordSnapshot) snapshot
        );
    }

    public static AuditRevisionResponse<LoanInstallmentSnapshot> toInstallmentResponse(
            Revision<Long, LoanInstallment> revision
    ) {
        LoanInstallment installment = revision.getEntity();
        LoanInstallmentSnapshot snapshot = new LoanInstallmentSnapshot(
                installment.getInstallmentNumber(),
                installment.getAmount(),
                installment.getAmountPaid(),
                installment.getDueDate(),
                installment.getRecordedStatus(),
                installment.getLoanRecord().getId()
        );
        return toResponse(revision, "LoanInstallment", installment.getId(), snapshot);
    }


    public static AuditRevisionResponse<LoanRecordSnapshot> toLoanResponse(
            Revision<Long, LoanRecord> revision
    ) {
        LoanRecord loan = revision.getEntity();
        LoanRecordSnapshot snapshot = new LoanRecordSnapshot(
                loan.getStatus(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTerm(),
                loan.getRejectionReason(),
                loan.getCreatedAt(),
                loan.getApprovedAt(),
                loan.getCustomer().getId(),
                loan.getCreatedBy() == null ? null : loan.getCreatedBy().getId(),
                loan.getApprovedBy() == null ? null : loan.getApprovedBy().getId()
        );
        return toResponse(revision, "LoanRecord", loan.getId(), snapshot);
    }


}
