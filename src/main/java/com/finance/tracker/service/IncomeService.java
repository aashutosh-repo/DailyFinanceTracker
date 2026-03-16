package com.finance.tracker.service;

import com.finance.tracker.dto.IncomeDto;
import java.util.List;

public interface IncomeService {
    IncomeDto createIncome(IncomeDto incomeDto, Long userId);
    IncomeDto updateIncome(Long incomeId, IncomeDto incomeDto);
    IncomeDto getIncomeById(Long incomeId);
    List<IncomeDto> getIncomeByUser(Long userId);
    void deleteIncome(Long incomeId);
}
