package com.bank.loan.loan_record_search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class LoanSearchController {
    private final LoanSearchService searchService;

    public LoanSearchController(LoanSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/api/loans/search")
    public Page<LoanSearchDTO> search(
            LoanSearchDTO criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {

        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return searchService.executeSearch(criteria, pageable);
    }
}