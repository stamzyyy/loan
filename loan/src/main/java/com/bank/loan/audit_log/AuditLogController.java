package com.bank.loan.audit_log;

import com.bank.loan.installments.LoanInstallmentRepository;
import com.bank.loan.loan_record.LoanRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.bank.loan.audit_log.AuditService;
// import  com.bank.loan.audit_log.LoanRecordSnapshot;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final LoanRecordRepository loanRecordRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
//    private final LoanRecordSnapshot loanRecordSnapshot;
private final AuditService auditService;

    public AuditLogController(
            LoanRecordRepository loanRecordRepository,
            LoanInstallmentRepository loanInstallmentRepository, LoanRecordSnapshot loanRecordSnapshot, AuditService auditService
    ) {
        this.loanRecordRepository = loanRecordRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
//        this.loanRecordSnapshot = loanRecordSnapshot;
        this.auditService = auditService;
    }

    @GetMapping("/loans/audit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<AuditRevisionResponse<LoanRecordSnapshot>>> getLoanHistory(
            @PathVariable long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(loanRecordRepository.findRevisions(id, pageable).map(revision -> auditService.toLoanResponse(revision)));
    }

    @GetMapping("/installments/audit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<AuditRevisionResponse<LoanInstallmentSnapshot>>> getInstallmentHistory(
            @PathVariable long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(loanInstallmentRepository.findRevisions(id, pageable).map(revision -> auditService.toInstallmentResponse(revision)));}


}
