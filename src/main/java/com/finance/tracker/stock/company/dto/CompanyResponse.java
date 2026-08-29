package com.finance.tracker.stock.company.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CompanyResponse {

    UUID id;

    String symbol;

    String name;

    String exchange;

    String sector;

    String industry;
}