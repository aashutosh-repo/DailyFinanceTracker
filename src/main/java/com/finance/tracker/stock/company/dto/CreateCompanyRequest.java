package com.finance.tracker.stock.company.dto;

import lombok.Data;

@Data
public class CreateCompanyRequest {

    private String symbol;

    private String name;

    private String exchange;

    private String sector;

    private String industry;
}