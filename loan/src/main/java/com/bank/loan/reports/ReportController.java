package com.bank.loan.reports;

import com.bank.loan.loan_record.LoanRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOAN_OFFICER', 'MANAGER', 'ADMIN')")
public class ReportController {
    private final LoanRecordService loanRecordService;

    @GetMapping("/loans/disbursed")
    public ResponseEntity<DisbursedLoansReport> disbursedLoans(
            @RequestParam(defaultValue = "today") String period
    ) {
        return ResponseEntity.ok(loanRecordService.getDisbursedLoansReport(period));
    }

    @GetMapping("/installments/overdue")
    public ResponseEntity<OverdueInstallmentsReport> overdueInstallments() {
        return ResponseEntity.ok(loanRecordService.getOverdueInstallmentsReport());
    }
}
