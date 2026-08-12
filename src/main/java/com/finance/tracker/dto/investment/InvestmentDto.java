package com.finance.tracker.dto.investment;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RequiredArgsConstructor
@Builder
public class InvestmentDto {
    private Long investmentId;
    private String investmentType;
    private String name;
    private String tickerSymbol;
    private String description;
    private LocalDate buyDate;
    private BigDecimal quantity;
    private BigDecimal buyPrice;
    private BigDecimal currentPrice;
    private String currency;
    private String brokerName;
    private String status;
    private String notes;
    private BigDecimal currentValue;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercentage;

}
