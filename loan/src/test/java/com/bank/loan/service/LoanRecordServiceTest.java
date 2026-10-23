package com.bank.loan.service;

import com.bank.loan.customer.Customer;
import com.bank.loan.installments.InstallmentStatus;
import com.bank.loan.installments.LoanInstallment;
import com.bank.loan.installments.PaymentReceipt;
import com.bank.loan.loan_record.LoanRecord;
import com.bank.loan.loan_record.LoanRecordService;
import com.bank.loan.loan_record.Status;
import com.bank.loan.customer.CustomerRepository;
import com.bank.loan.installments.LoanInstallmentRepository;
import com.bank.loan.loan_record.LoanRecordRepository;
import com.bank.loan.user.User;
import com.bank.loan.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanRecordServiceTest {
    @Mock
    private LoanRecordRepository loanRecordRepository;
    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private LoanRecordService loanRecordService;

    @Test
    void approvalGeneratesMonthlyInstallmentsWithFlatInterest() {
        Customer customer = new Customer();
        customer.setId(1L);
        User officer = user(1L, "officer");
        User manager = user(2L, "manager");
        LoanRecord loan = new LoanRecord(customer, new BigDecimal("1000.00"),
                new BigDecimal("12"), 3, officer);
        loan.setId(10L);
        when(loanRecordRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(manager));
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(customer));
        when(loanRecordRepository.countByCustomerIdAndStatusIn(eq(1L), anyCollection())).thenReturn(0L);
        when(loanRecordRepository.save(loan)).thenReturn(loan);

        LoanRecord approvedLoan = loanRecordService.approveLoan(10L, "manager");

        assertEquals(Status.APPROVED, approvedLoan.getStatus());
        assertEquals(3, approvedLoan.getInstallments().size());
        assertEquals(new BigDecimal("1120.00"), approvedLoan.getInstallments().stream()
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(LocalDate.now(ZoneOffset.UTC).plusMonths(1),
                approvedLoan.getInstallments().get(0).getDueDate());
        assertTrue(approvedLoan.getInstallments().stream()
                .allMatch(installment -> installment.getStatus() == InstallmentStatus.PENDING));
    }

    @Test
    void paymentIsAllocatedOldestLoanFirstAndClosesOnlyTheFullyPaidLoan() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setHasLoan(true);
        LoanRecord oldestLoan = loan(10L, customer);
        LoanRecord newerLoan = loan(11L, customer);
        LoanInstallment oldestInstallment = installment(oldestLoan, 1, "10.00");
        LoanInstallment newerInstallment = installment(newerLoan, 1, "20.00");
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(customer));
        when(loanInstallmentRepository.findOutstandingForCustomer(
                eq(1L), anyCollection(), anyCollection()))
                .thenReturn(List.of(oldestInstallment, newerInstallment));
        when(loanInstallmentRepository.countByLoanRecordIdAndStatusNot(10L, InstallmentStatus.PAID))
                .thenReturn(0L);
        when(loanInstallmentRepository.countByLoanRecordIdAndStatusNot(11L, InstallmentStatus.PAID))
                .thenReturn(1L);
        when(loanRecordRepository.countByCustomerIdAndStatusIn(eq(1L), anyCollection())).thenReturn(1L);

        PaymentReceipt receipt = loanRecordService.recordPayment(1L, new BigDecimal("15.00"));

        assertEquals(Status.CLOSED, oldestLoan.getStatus());
        assertEquals(Status.APPROVED, newerLoan.getStatus());
        assertTrue(customer.getHasLoan());
        assertEquals(new BigDecimal("10.00"), receipt.allocations().get(0).amountApplied());
        assertEquals(new BigDecimal("5.00"), receipt.allocations().get(1).amountApplied());
        assertEquals(InstallmentStatus.PAID, receipt.allocations().get(0).installmentStatus());
        assertEquals(new BigDecimal("15.00"), receipt.allocations().get(1).remainingInstallmentAmount());
    }

    @Test
    void unpaidInstallmentIsReportedOverdueAfterItsDueDate() {
        LoanInstallment installment = new LoanInstallment(
                loan(10L, new Customer()), 1, new BigDecimal("10.00"),
                LocalDate.now(ZoneOffset.UTC).minusDays(1));

        assertEquals(InstallmentStatus.OVERDUE, installment.getStatus());

        installment.setStatus(InstallmentStatus.PAID);
        assertEquals(InstallmentStatus.PAID, installment.getStatus());
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private static LoanRecord loan(Long id, Customer customer) {
        LoanRecord loan = new LoanRecord(customer, BigDecimal.ONE, BigDecimal.ZERO, 1, null);
        loan.setId(id);
        loan.setStatus(Status.APPROVED);
        return loan;
    }

    private static LoanInstallment installment(LoanRecord loan, int number, String amount) {
        LoanInstallment installment = new LoanInstallment(
                loan, number, new BigDecimal(amount), LocalDate.now().plusMonths(number));
        loan.addInstallment(installment);
        return installment;
    }
}
