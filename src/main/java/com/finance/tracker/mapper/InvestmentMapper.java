package com.finance.tracker.mapper;

import com.finance.tracker.dto.investment.InvestmentDto;
import com.finance.tracker.entity.Investment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InvestmentMapper {
    public InvestmentDto toDto(Investment investment) {
        if( investment == null) return null;

        BigDecimal currentValue = investment.getQuantity().multiply(investment.getCurrentValue());
        BigDecimal initialCost = investment.getQuantity().multiply(investment.getBuyPrice());
        BigDecimal gainLoss = currentValue.subtract(initialCost);
        BigDecimal gainLossPercentage = initialCost.compareTo(BigDecimal.ZERO) > 0
        ? gainLoss.multiply(BigDecimal.valueOf(100))
          .divide(initialCost,2, RoundingMode.HALF_UP)
          :BigDecimal.ZERO;

        return InvestmentDto.builder()
                .investmentId(investment.getId())
                .investmentType(investment.getInvestmentType())
                .tickerSymbol(investment.getTickerSymbol())
                .description(investment.getDescription())
                .buyDate(investment.getBuyDate())
                .quantity(investment.getQuantity())
                .buyPrice(investment.getBuyPrice())
                .currentPrice(investment.getCurrentPrice())
                .currency(investment.getCurrency())
                .brokerName(investment.getBrokerName())
                .status(investment.getStatus())
                .name(investment.getNotes())
                .currentValue(currentValue)
                .gainLossPercentage(gainLossPercentage)
                .gainLoss(gainLoss)
                .build();
    }

    public Investment toEntity( InvestmentDto dto) {
        if (dto == null)  return null;
        return Investment.builder()
                .id(dto.getInvestmentId())
                .investmentType(dto.getInvestmentType())
                .tickerSymbol(dto.getTickerSymbol())
                .description(dto.getDescription())
                .buyDate(dto.getBuyDate())
                .quantity(dto.getQuantity())
                .buyPrice(dto.getBuyPrice())
                .currentPrice(dto.getCurrentPrice())
                .currency(dto.getCurrency())
                .brokerName(dto.getBrokerName())
                .status(dto.getStatus())
                .name(dto.getNotes())
                .build();
    }
}
