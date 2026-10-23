package com.bank.loan.bulk;

import com.bank.loan.customer.CustomerRequest;
import com.bank.loan.customer.CustomerResponse;
import com.bank.loan.customer.CustomerService;
import com.bank.loan.exceptions.BusinessRuleViolationException;
import com.bank.loan.exceptions.DuplicateResourceException;
import com.bank.loan.exceptions.ResourceNotFoundException;
import com.bank.loan.loan_record.LoanRecord;
import com.bank.loan.loan_record.LoanRecordService;
import com.bank.loan.loan_record.LoanRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkCsvImportService {
    private static final List<String> CUSTOMER_HEADERS = List.of("fullname", "nationalid", "phone", "email");
    private static final List<String> LOAN_HEADERS = List.of("nationalid", "amount", "interestrate", "term");

    private final CustomerService customerService;
    private final LoanRecordService loanRecordService;
    private final Validator validator;

    public CsvImportResult importCustomers(MultipartFile file) {
        List<List<String>> records = readRecords(file);
        Map<String, Integer> headers = validateHeaders(records.get(0), CUSTOMER_HEADERS);
        List<CsvImportRow> results = new ArrayList<>();

        for (int index = 1; index < records.size(); index++) {
            int rowNumber = index + 1;
            List<String> row = records.get(index);
            try {
                requireCellCount(row, CUSTOMER_HEADERS.size());
                CustomerRequest request = new CustomerRequest(
                        value(row, headers, "fullname"),
                        value(row, headers, "nationalid"),
                        value(row, headers, "phone"),
                        value(row, headers, "email"));
                String violations = validationErrors(request);
                if (violations != null) {
                    results.add(failed(rowNumber, violations));
                    continue;
                }
                CustomerResponse customer = customerService.create(request);
                results.add(imported(rowNumber, customer));
            } catch (DuplicateResourceException | BusinessRuleViolationException | IllegalArgumentException ex) {
                results.add(failed(rowNumber, ex.getMessage()));
            }
        }
        return summarize(results);
    }

    public CsvImportResult importLoans(MultipartFile file, String username) {
        List<List<String>> records = readRecords(file);
        Map<String, Integer> headers = validateHeaders(records.get(0), LOAN_HEADERS);
        List<CsvImportRow> results = new ArrayList<>();

        for (int index = 1; index < records.size(); index++) {
            int rowNumber = index + 1;
            List<String> row = records.get(index);
            try {
                requireCellCount(row, LOAN_HEADERS.size());
                LoanRequestDTO request = new LoanRequestDTO(
                        value(row, headers, "nationalid"),
                        new BigDecimal(value(row, headers, "amount")),
                        new BigDecimal(value(row, headers, "interestrate")),
                        Integer.valueOf(value(row, headers, "term")));
                String violations = validationErrors(request);
                if (violations != null) {
                    results.add(failed(rowNumber, violations));
                    continue;
                }
                LoanRecord loan = loanRecordService.createLoanRecord(request, username);
                results.add(imported(rowNumber, new ImportedLoan(
                        loan.getId(),
                        loan.getCustomer().getNationalId(),
                        loan.getAmount(),
                        loan.getInterestRate(),
                        loan.getTerm(),
                        loan.getStatus().name())));
            } catch (DuplicateResourceException | BusinessRuleViolationException |
                     ResourceNotFoundException ex) {
                results.add(failed(rowNumber, ex.getMessage()));
            } catch (IllegalArgumentException ex) {
                results.add(failed(rowNumber, ex.getMessage()));
            }
        }
        return summarize(results);
    }

    private List<List<String>> readRecords(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a non-empty CSV file");
        }
        try {
            String csv = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (!csv.isEmpty() && csv.charAt(0) == '\uFEFF') {
                csv = csv.substring(1);
            }
            return CsvParser.parse(csv);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read uploaded CSV file", ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private Map<String, Integer> validateHeaders(List<String> row, List<String> expectedHeaders) {
        Map<String, Integer> headers = new HashMap<>();
        for (int index = 0; index < row.size(); index++) {
            String header = row.get(index).toLowerCase(Locale.ROOT);
            if (headers.putIfAbsent(header, index) != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "CSV contains duplicate header: " + row.get(index));
            }
        }
        if (!headers.keySet().equals(new HashSet<>(expectedHeaders))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CSV headers must be exactly: " + String.join(",", expectedHeaders));
        }
        return headers;
    }

    private String validationErrors(Object request) {
        Set<ConstraintViolation<Object>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
    }

    private static void requireCellCount(List<String> row, int expectedCount) {
        if (row.size() != expectedCount) {
            throw new IllegalArgumentException(
                    "Expected " + expectedCount + " columns but found " + row.size());
        }
    }

    private static String value(List<String> row, Map<String, Integer> headers, String header) {
        return row.get(headers.get(header));
    }

    private static CsvImportRow imported(int rowNumber, Object data) {
        return new CsvImportRow(rowNumber, "IMPORTED", "Imported successfully", data);
    }

    private static CsvImportRow failed(int rowNumber, String message) {
        return new CsvImportRow(rowNumber, "FAILED", message, null);
    }

    private static CsvImportResult summarize(List<CsvImportRow> rows) {
        int imported = (int) rows.stream().filter(row -> row.status().equals("IMPORTED")).count();
        return new CsvImportResult(rows.size(), imported, rows.size() - imported, rows);
    }

    public record ImportedLoan(
            Long id,
            String customerNationalId,
            BigDecimal amount,
            BigDecimal interestRate,
            Integer term,
            String status
    ) {
    }
}
