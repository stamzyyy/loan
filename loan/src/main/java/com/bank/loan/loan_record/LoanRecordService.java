package com.bank.loan.loan_record;

import com.bank.loan.customer.Customer;
import com.bank.loan.customer.CustomerRepository;
import com.bank.loan.exceptions.BusinessRuleViolationException;
import com.bank.loan.exceptions.ResourceNotFoundException;
import com.bank.loan.installments.InstallmentStatus;
import com.bank.loan.installments.LoanInstallment;
import com.bank.loan.installments.LoanInstallmentRepository;
import com.bank.loan.installments.PaymentReceipt;
import com.bank.loan.reports.DisbursedLoanItem;
import com.bank.loan.reports.DisbursedLoansReport;
import com.bank.loan.reports.OverdueInstallmentItem;
import com.bank.loan.reports.OverdueInstallmentsReport;
import com.bank.loan.user.User;
import com.bank.loan.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LoanRecordService {
    private static final List<Status> ACTIVE_LOAN_STATUSES = List.of(Status.APPROVED, Status.ACTIVE);
    private static final List<InstallmentStatus> UNPAID_INSTALLMENT_STATUSES =
            List.of(InstallmentStatus.PENDING, InstallmentStatus.OVERDUE);
    private final LoanRecordRepository loanRecordRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;


    @Transactional
    public LoanRecord createLoanRecord(LoanRequestDTO loanRequest, String username) {
        Customer customer = customerRepository.findByNationalId(loanRequest.nationalId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + loanRequest.nationalId()));
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        ensureCustomerCanHaveAnotherActiveLoan(customer.getId());

        LoanRecord loanRecord = new LoanRecord(customer, loanRequest.amount(), loanRequest.interestRate(), loanRequest.term(), currentUser);
        return loanRecordRepository.save(loanRecord);
    }

    @Transactional(readOnly = true)
    public LoanRecord getLoanRecord(Long id) {
        LoanRecord loanRecord = loanRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan record not found: " + id));
        loanRecord.getInstallments().size();
        return loanRecord;
    }

    @Transactional(readOnly = true)
    public Page<LoanRecord> searchLoanRecords(String nationalId, Pageable pageable) {
        Page<LoanRecord> loanRecords = nationalId == null || nationalId.isBlank()
                ? loanRecordRepository.findAll(pageable)
                : loanRecordRepository.findByCustomerNationalIdContainingIgnoreCase(nationalId, pageable);
        loanRecords.forEach(loanRecord -> loanRecord.getInstallments().size());
        return loanRecords;
    }

    @Transactional
    public LoanRecord approveLoan(Long id, String username) {
        LoanRecord loanRecord = getLoanRecord(id);
        User checker = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (loanRecord.getCreatedBy() != null
                && Objects.equals(loanRecord.getCreatedBy().getId(), checker.getId())) {
            throw new BusinessRuleViolationException("The user who created a loan cannot approve it");
        }

        Customer customer = customerRepository.findByIdForUpdate(loanRecord.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + loanRecord.getCustomer().getId()));
        ensureCustomerCanHaveAnotherActiveLoan(customer.getId());
        loanRecord.approve(checker);
        loanRecord.addInstallments(loanRecord);
        return loanRecordRepository.save(loanRecord);
    }

    @Transactional
    public LoanRecord rejectLoan(Long id, String username, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new BusinessRuleViolationException("Rejection reason is required");
        }

        LoanRecord loanRecord = getLoanRecord(id);
        User checker = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        loanRecord.reject(checker, rejectionReason.trim());
        return loanRecordRepository.save(loanRecord);
    }

    public void ensureCustomerCanHaveAnotherActiveLoan(Long customerId) {
        long activeLoanCount = loanRecordRepository.countByCustomerIdAndStatusIn(customerId, ACTIVE_LOAN_STATUSES);
        if (activeLoanCount >= 2) {
            throw new BusinessRuleViolationException("A customer cannot have more than 2 active loans");
        }
    }


    @Transactional
    public PaymentReceipt recordPayment(Long customerId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new BusinessRuleViolationException("Payment amount must be positive and have at most 2 decimal places");
        }

        Customer customer = customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        List<LoanInstallment> outstanding = loanInstallmentRepository.findOutstandingForCustomer(
                customerId, ACTIVE_LOAN_STATUSES, UNPAID_INSTALLMENT_STATUSES);
        if (outstanding.isEmpty()) {
            throw new BusinessRuleViolationException("The customer has no outstanding installments");
        }

        BigDecimal totalOutstanding = outstanding.stream()
                .map(LoanInstallment::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.compareTo(totalOutstanding) > 0) {
            throw new BusinessRuleViolationException(
                    "Payment exceeds the customer's outstanding balance of " + totalOutstanding);
        }

        BigDecimal remainingPayment = amount;
        Map<LoanInstallment, BigDecimal> appliedInstallments = new LinkedHashMap<>();
        Set<LoanRecord> affectedLoans = new LinkedHashSet<>();

        for (LoanInstallment installment : outstanding) {
            if (remainingPayment.signum() == 0) {
                break;
            }

            BigDecimal applied = remainingPayment.min(installment.getRemainingAmount());
            installment.applyPayment(applied);
            remainingPayment = remainingPayment.subtract(applied);
            appliedInstallments.put(installment, applied);
            affectedLoans.add(installment.getLoanRecord());
        }

        loanInstallmentRepository.saveAll(outstanding);
        List<LoanRecord> completedLoans = affectedLoans.stream()
                .filter(loanRecord -> loanInstallmentRepository.countByLoanRecordIdAndStatusNot(
                        loanRecord.getId(), InstallmentStatus.PAID) == 0)
                .toList();
        completedLoans.forEach(LoanRecord::close);
        loanRecordRepository.saveAll(completedLoans);
        if (!completedLoans.isEmpty()) {
            customer.setHasLoan(loanRecordRepository.countByCustomerIdAndStatusIn(
                    customerId, ACTIVE_LOAN_STATUSES) > 0);
            customerRepository.save(customer);
        }
        List<PaymentReceipt.Allocation> allocations = appliedInstallments.entrySet().stream()
                .map(entry -> {
                    LoanInstallment installment = entry.getKey();
                    return new PaymentReceipt.Allocation(
                            installment.getLoanRecord().getId(),
                            installment.getInstallmentNumber(),
                            entry.getValue(),
                            installment.getRemainingAmount(),
                            installment.getStatus(),
                            installment.getLoanRecord().getStatus(),
                            installment.getDueDate()
                    );
                })
                .toList();
        return new PaymentReceipt(amount, allocations);
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "GMT+2")
    @Transactional
    public void markOverdueInstallments() {
        List<LoanInstallment> overdue = loanInstallmentRepository.findByStatusAndDueDateBefore(
                InstallmentStatus.PENDING, LocalDate.now(ZoneId.of("GMT+2")));
        overdue.forEach(installment -> installment.setStatus(InstallmentStatus.OVERDUE));
        loanInstallmentRepository.saveAll(overdue);
    }

    @Transactional(readOnly = true)
    public DisbursedLoansReport getDisbursedLoansReport(String period) {
        LocalDate[] dates = LoanRecord.getStartDateAndEndDate(period);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];

        List<LoanRecord> loans = loanRecordRepository
                .findByApprovedAtGreaterThanEqualAndApprovedAtLessThanOrderByApprovedAtDesc(
                        startDate.atStartOfDay(ZoneId.of("GMT+2")).toLocalDate(),
                        endDate.atStartOfDay(ZoneId.of("GMT+2")).toLocalDate());
        List<DisbursedLoanItem> items = loans
                .stream()
                .map(loan -> new DisbursedLoanItem(
                        loan.getId(),
                        loan.getCustomer().getNationalId(),
                        loan.getCustomer().getFullName(),
                        loan.getAmount(),
                        loan.getInterestRate(),
                        loan.getTerm(),
                        loan.getApprovedAt(),
                        loan.getApprovedBy() == null ? null : loan.getApprovedBy().getUsername()))
                .toList();
        BigDecimal totalPrincipal = loans.stream()
                .map(LoanRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DisbursedLoansReport(
                period, startDate, endDate.minusDays(1), items.size(), totalPrincipal, items);
    }


    public OverdueInstallmentsReport getOverdueInstallmentsReport() {
        LocalDate today = LocalDate.now(ZoneId.of("GMT+2"));
        List<LoanInstallment> overdue = loanInstallmentRepository.findOverdueInstallments(
                today,
                List.of(InstallmentStatus.PENDING, InstallmentStatus.OVERDUE),
                ACTIVE_LOAN_STATUSES);
        List<OverdueInstallmentItem> items = overdue
                .stream()
                .map(installment -> new OverdueInstallmentItem(
                        installment.getId(),
                        installment.getLoanRecord().getId(),
                        installment.getLoanRecord().getCustomer().getNationalId(),
                        installment.getLoanRecord().getCustomer().getFullName(),
                        installment.getInstallmentNumber(),
                        installment.getDueDate(),
                        installment.getAmount(),
                        installment.getAmountPaid(),
                        installment.getRemainingAmount()))
                .toList();
        BigDecimal totalOutstanding = items
                .stream()
                .map(OverdueInstallmentItem::outstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OverdueInstallmentsReport(today, items.size(), totalOutstanding, items);
    }


}
