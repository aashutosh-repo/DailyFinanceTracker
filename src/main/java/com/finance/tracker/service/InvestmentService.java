package com.finance.tracker.service;

import com.finance.tracker.dto.investment.InvestmentDto;
import com.finance.tracker.entity.Investment;

import java.math.BigDecimal;
import java.util.List;

public interface InvestmentService {
    InvestmentDto createInvestment(String userId, InvestmentDto dto);
    InvestmentDto getInvestmentByUserId(String userId);
    List<InvestmentDto> getUserInvestments(String userId);
    List<InvestmentDto> getUserActiveInvestments(String userId);
    InvestmentDto updateInvestment(Long id, InvestmentDto dto);
    void deleteInvestment(Long id);
    InvestmentDto updateCurrentPrice(Long id, BigDecimal currentPrice);
}
