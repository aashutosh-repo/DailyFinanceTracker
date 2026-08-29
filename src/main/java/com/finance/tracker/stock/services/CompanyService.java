package com.finance.tracker.stock.services;

import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.company.dto.CompanyResponse;
import com.finance.tracker.stock.company.dto.CreateCompanyRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;


    public CompanyResponse createCompany(CreateCompanyRequest request) {

        if (companyRepository.existsBySymbol(request.getSymbol())) {
            throw new RuntimeException("Company already exists: " + request.getSymbol());
        }


        Company company = Company.builder()
                .symbol(request.getSymbol().toUpperCase())
                .name(request.getName())
                .exchange(request.getExchange())
                .sector(request.getSector())
                .industry(request.getIndustry())
                .build();


        Company savedCompany = companyRepository.save(company);


        return mapToResponse(savedCompany);
    }


    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {

        return companyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public CompanyResponse getBySymbol(
            String symbol
    ) {

        Company company = companyRepository.findBySymbol(symbol.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Company not found: " + symbol));


        return mapToResponse(company);
    }


    private CompanyResponse mapToResponse(
            Company company
    ) {

        return CompanyResponse.builder()
                .id(company.getId())
                .symbol(company.getSymbol())
                .name(company.getName())
                .exchange(company.getExchange())
                .sector(company.getSector())
                .industry(company.getIndustry())
                .build();
    }
}