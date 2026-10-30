package com.bank.loan.customer;

import com.bank.loan.exceptions.DuplicateResourceException;
import com.bank.loan.exceptions.ResourceNotFoundException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public String uploadCustomers(MultipartFile file) throws IOException {
        Set<Customer> students = parseCsv(file);

        customerRepository.saveAll(students);
        return students.size() + " were successfully uploaded!";
    }

    private Set<Customer> parseCsv(MultipartFile file) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<CustomerCsvRepresentation> strategy = new HeaderColumnNameMappingStrategy<>();

            strategy.setType(CustomerCsvRepresentation.class);
            CsvToBean<CustomerCsvRepresentation> csvToBean = new CsvToBeanBuilder<CustomerCsvRepresentation>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

          return csvToBean.parse()
                    .stream()
                    .map(csvLine -> Customer.builder()
                            .fullName(csvLine.getFullName())
                            .nationalId(csvLine.getNationalId())
                            .phone(csvLine.getPhone())
                            .email(csvLine.getEmail())
                            .build()

                    )
                    .collect(Collectors.toSet());


        }

    }
}