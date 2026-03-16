package com.finance.tracker.service.impl;

import com.finance.tracker.dto.IncomeDto;
import com.finance.tracker.entity.Income;
import com.finance.tracker.entity.User;
import com.finance.tracker.repository.IncomeRepository;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    @Override
    public IncomeDto createIncome(IncomeDto incomeDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Income income = Income.builder()
                .user(user)
                .sourceType(incomeDto.getSourceType())
                .amount(incomeDto.getAmount())
                .incomeDate(incomeDto.getIncomeDate())
                .currency(incomeDto.getCurrency() != null ? incomeDto.getCurrency() : "USD")
                .description(incomeDto.getDescription())
                .isRecurring(false)
                .build();
        
        Income saved = incomeRepository.save(income);
        return mapToDto(saved);
    }

    @Override
    public IncomeDto updateIncome(Long incomeId, IncomeDto incomeDto) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));
        
        income.setSourceType(incomeDto.getSourceType());
        income.setAmount(incomeDto.getAmount());
        income.setIncomeDate(incomeDto.getIncomeDate());
        income.setCurrency(incomeDto.getCurrency());
        income.setDescription(incomeDto.getDescription());
        income.setUpdatedAt(LocalDateTime.now());
        
        Income updated = incomeRepository.save(income);
        return mapToDto(updated);
    }

    @Override
    public IncomeDto getIncomeById(Long incomeId) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));
        return mapToDto(income);
    }

    @Override
    public List<IncomeDto> getIncomeByUser(Long userId) {
        List<Income> incomes = incomeRepository.findAll().stream()
                .filter(i -> i.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        return incomes.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteIncome(Long incomeId) {
        if (!incomeRepository.existsById(incomeId)) {
            throw new RuntimeException("Income not found");
        }
        incomeRepository.deleteById(incomeId);
    }

    private IncomeDto mapToDto(Income income) {
        return IncomeDto.builder()
                .id(income.getId())
                .sourceType(income.getSourceType())
                .amount(income.getAmount())
                .incomeDate(income.getIncomeDate())
//                .category(income.getCategory())
                .currency(income.getCurrency())
                .description(income.getDescription())
//                .status(income.getStatus())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }
}
