package com.bank.loan.loan_record;

import com.bank.loan.bulk.BulkCsvImportService;
import com.bank.loan.bulk.CsvImportResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoanRecordController {

    private final LoanRecordService loanRecordService;
    private final BulkCsvImportService bulkCsvImportService;

    @PostMapping("/loan-record/create")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<LoanRecord> createLoanRecord(
            @Valid @RequestBody LoanRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanRecordService.createLoanRecord(request, authentication.getName()));
    }

    @PostMapping(value = "/loan-records/bulk-upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<CsvImportResult> bulkUpload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return ResponseEntity.ok(bulkCsvImportService.importLoans(file, authentication.getName()));
    }

    @GetMapping("/loan-records/{id}")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<LoanRecord> getLoanRecord(@PathVariable Long id) {
        return ResponseEntity.ok(loanRecordService.getLoanRecord(id));
    }

    @GetMapping("/loan-records")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<LoanRecord>> searchLoanRecords(
            @RequestParam(required = false) String nationalId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(loanRecordService.searchLoanRecords(nationalId, pageable));
    }

    @PutMapping("/loan-records/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LoanRecord> approveLoanRecord(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(loanRecordService.approveLoan(id, authentication.getName()));
    }

    @PutMapping("/loan-records/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LoanRecord> rejectLoanRecord(
            @PathVariable Long id,
            @Valid @RequestBody LoanRejectionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                loanRecordService.rejectLoan(id, authentication.getName(), request.rejectionReason()));
    }
}
