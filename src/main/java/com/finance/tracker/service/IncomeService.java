package com.finance.tracker.service;

import com.finance.tracker.dto.IncomeDto;
import java.util.List;

public interface IncomeService {
    IncomeDto createIncome(IncomeDto incomeDto, String userId);
    IncomeDto updateIncome(Long incomeId, IncomeDto incomeDto);
    IncomeDto getIncomeById(Long incomeId);
    List<IncomeDto> getIncomeByUser(String userId);
    void deleteIncome(Long incomeId);
}
