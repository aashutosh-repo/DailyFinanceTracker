package com.finance.tracker.service.impl;

import com.finance.tracker.dto.investment.InvestmentDto;
import com.finance.tracker.mapper.InvestmentMapper;
import com.finance.tracker.repository.InvestmentRepository;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final InvestmentMapper mapper;

    @Override
    public InvestmentDto createInvestment(String userId, InvestmentDto dto) {
        return null;
    }

    @Override
    public InvestmentDto getInvestmentByUserId(String userId) {
        return null;
    }

    @Override
    public List<InvestmentDto> getUserInvestments(String userId) {
        return List.of();
    }

    @Override
    public List<InvestmentDto> getUserActiveInvestments(String userId) {
        return List.of();
    }

    @Override
    public InvestmentDto updateInvestment(Long id, InvestmentDto dto) {
        return null;
    }

    @Override
    public void deleteInvestment(Long id) {

    }

    @Override
    public InvestmentDto updateCurrentPrice(Long id, BigDecimal currentPrice) {
        return null;
    }
}
