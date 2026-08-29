package com.finance.tracker.stock.market.controller;


import com.finance.tracker.stock.company.dto.CompanyResponse;
import com.finance.tracker.stock.company.dto.CreateCompanyRequest;
import com.finance.tracker.stock.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("Companies Controller")
@RequestMapping("/api/stocks/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse createCompany(
            @RequestBody CreateCompanyRequest request
    ) {

        return companyService.createCompany(request);
    }


    @GetMapping
    public List<CompanyResponse> getAllCompanies() {

        return companyService.getAllCompanies();
    }


    @GetMapping("/{symbol}")
    public CompanyResponse getCompany(
            @PathVariable String symbol
    ) {

        return companyService.getBySymbol(symbol);
    }
}