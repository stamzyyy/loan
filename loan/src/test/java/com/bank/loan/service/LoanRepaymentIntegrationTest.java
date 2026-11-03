package com.bank.loan.service;

import com.bank.loan.customer.Customer;
import com.bank.loan.customer.CustomerRepository;
import com.bank.loan.installments.InstallmentStatus;
import com.bank.loan.installments.PaymentReceipt;
import com.bank.loan.loan_record.LoanRecord;
import com.bank.loan.loan_record.LoanRecordService;
import com.bank.loan.loan_record.LoanRequestDTO;
import com.bank.loan.loan_record.Status;
import com.bank.loan.user.Role;
import com.bank.loan.user.User;
import com.bank.loan.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("prod")
class LoanRepaymentIntegrationTest {
    private static LoanRecordService loanRecordService;
    private static CustomerRepository customerRepository;
    private static UserRepository userRepository;

    @Autowired
    public LoanRepaymentIntegrationTest(LoanRecordService loanRecordService, CustomerRepository customerRepository, UserRepository userRepository) {
        LoanRepaymentIntegrationTest.loanRecordService = loanRecordService;
        LoanRepaymentIntegrationTest.customerRepository = customerRepository;
        LoanRepaymentIntegrationTest.userRepository = userRepository;
    }

    @Test
    void approvalAndFullRepaymentPersistInstallmentsAndCloseLoan() {
        String suffix = UUID.randomUUID().toString();
        Customer customer = new Customer();
        customer.setFullName("Tadiwa Manyakaidze");
        customer.setNationalId(suffix);
        customer.setPhone("0771465542");
        customer.setEmail(suffix + "tadiwa@gmail.com");
        customer = customerRepository.save(customer);

        User officer = createUser("officer-" + suffix, Role.LOAN_OFFICER);
        User manager = createUser("manager-" + suffix, Role.MANAGER);
        LoanRecord loan = loanRecordService.createLoanRecord(
                new LoanRequestDTO(suffix, new BigDecimal("1000.00"), BigDecimal.ZERO, 2),
                officer.getUsername());
        loan = loanRecordService.approveLoan(loan.getId(), manager.getUsername());

        assertEquals(2, loan.getInstallments().size());
        assertEquals(LocalDate.now(ZoneId.of("GMT+2")).plusMonths(1), loan.getInstallments().get(0).getDueDate());

        PaymentReceipt receipt = loanRecordService.recordPayment(customer.getId(), new BigDecimal("1000.00"));
        LoanRecord closedLoan = loanRecordService.getLoanRecord(loan.getId());

        assertEquals(2, receipt.allocations().size());
        assertEquals(Status.CLOSED, closedLoan.getStatus());
        assertTrue(closedLoan.getInstallments().stream()
                .allMatch(installment -> installment.getStatus() == InstallmentStatus.PAID));
        assertFalse(customerRepository.findById(customer.getId()).orElseThrow().getHasLoan());
    }

    private User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setRole(role);
        return userRepository.save(user);
    }
}
