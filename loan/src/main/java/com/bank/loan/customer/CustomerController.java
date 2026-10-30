package com.bank.loan.customer;



import com.bank.loan.installments.PaymentReceipt;
import com.bank.loan.installments.PaymentRequest;
import com.bank.loan.loan_record.LoanRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

     CustomerService customerService;
     LoanRecordService loanRecordService;


    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @PostMapping(value = "/bulk-upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<String> bulkUpload(@RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(customerService.uploadCustomers(file));
    }

    @PutMapping("update/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @GetMapping("get-id/{id}")
    public ResponseEntity<CustomerResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getId(id));
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> search(
            @RequestParam(defaultValue = "") String q,
            Pageable pageable
    ) {
        return ResponseEntity.ok(customerService.search(q, pageable));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/payments")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PaymentReceipt> recordPayment(
            @PathVariable Long customerId,
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(loanRecordService.recordPayment(customerId, request.amount()));
    }
}
