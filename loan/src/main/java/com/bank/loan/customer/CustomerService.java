package com.bank.loan.customer;

import com.bank.loan.exceptions.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bank.loan.customer.Customer.*;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;


    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByNationalId(request.nationalId())) {
            throw new DuplicateResourceException(
                    "National ID already exists: " + request.nationalId());
        }

        Customer customer = new Customer();
        apply(customer, request);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = find(id);
        if (customerRepository.existsByNationalIdAndIdNot(request.nationalId(), id)) {
            throw new DuplicateResourceException(
                    "National ID already exists: " + request.nationalId());
        }

        apply(customer, request);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse getId(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public Page<CustomerResponse> search(String query, Pageable pageable) {
        return customerRepository
                .findByFullNameContainingIgnoreCaseOrNationalIdContainingIgnoreCase(
                        query, query, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.delete(find(id));
    }


    private Customer find(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private void apply(Customer customer, CustomerRequest request) {
        customer.setFullName(request.fullName());
        customer.setNationalId(request.nationalId());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getNationalId(),
                customer.getEmail(),
                customer.getPhone());
    }

}
