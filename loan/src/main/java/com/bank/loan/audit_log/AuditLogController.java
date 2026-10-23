package com.bank.loan.audit_log;

import com.bank.loan.installments.LoanInstallment;
import com.bank.loan.installments.LoanInstallmentRepository;
import com.bank.loan.loan_record.LoanRecord;
import com.bank.loan.loan_record.LoanRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final LoanRecordRepository loanRecordRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

    public AuditLogController(
            LoanRecordRepository loanRecordRepository,
            LoanInstallmentRepository loanInstallmentRepository
    ) {
        this.loanRecordRepository = loanRecordRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    @GetMapping("/loan/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<AuditRevisionResponse<LoanRecordSnapshot>> getLoanHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return loanRecordRepository.findRevisions(id, pageable).map(AuditLogController::toLoanResponse);
    }

    @GetMapping("/installment/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<AuditRevisionResponse<LoanInstallmentSnapshot>> getInstallmentHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return loanInstallmentRepository.findRevisions(id, pageable).map(AuditLogController::toInstallmentResponse);
    }

    private static AuditRevisionResponse<LoanRecordSnapshot> toLoanResponse(
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

    private static AuditRevisionResponse<LoanInstallmentSnapshot> toInstallmentResponse(
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

    private static <T> AuditRevisionResponse<T> toResponse(
            Revision<Long, ?> revision,
            String entityName,
            Long entityId,
            T snapshot
    ) {
        var metadata = revision.getMetadata();
        return new AuditRevisionResponse<>(
                metadata.getRevisionNumber().orElseThrow(),
                metadata.getRevisionInstant().map(Object::toString).orElse(null),
                metadata.getRevisionType().name(),
                entityName,
                entityId,
                snapshot
        );
    }
}
