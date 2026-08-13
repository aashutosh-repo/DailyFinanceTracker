package com.finance.tracker.service.impl;

import com.finance.tracker.dto.investment.InvestmentDto;
import com.finance.tracker.entity.Investment;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.mapper.InvestmentMapper;
import com.finance.tracker.repository.InvestmentRepository;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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
    private final InvestmentMapper investmentMapper;

    @Override
    public InvestmentDto createInvestment(String userId, InvestmentDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Investment investment  = investmentMapper.toEntity(dto);

        Investment saved = investmentRepository.save(investment);
        log.info("Investment Created with id : {}", saved.getId());
        return investmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentDto getInvestmentById(Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found with id :" + id));
        return investmentMapper.toDto(investment);
    }

    @Override
    public List<InvestmentDto> getUserInvestments(String userId) {
        log.debug("fetching all investment for user : {} ", userId);
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("User Not found"));
        List<Investment> investments = investmentRepository.findByExtUserIdAndDeletedAtIsNull(userId, Pageable.unpaged()).getContent();

        return investments.stream().map(investmentMapper::toDto).toList();
    }

    @Override
    public List<InvestmentDto> getUserActiveInvestments(String userId) {
        log.debug("fetching Active investment for user : {} ", userId);
        List<Investment> investments = investmentRepository.findActiveInvestmentsByUserId(userId);

        return investments.stream().map(investmentMapper::toDto).toList();
    }

    @Override
    public InvestmentDto updateInvestment(Long id, InvestmentDto dto) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Investment found with Id: "+ id));

        Investment investment1 = investmentMapper.toEntity(dto);
        investment1.setId(investment.getId());
        Investment updated = investmentRepository.save(investment1);

        return investmentMapper.toDto(updated);
    }

    @Override
    public void deleteInvestment(Long id) {
        log.debug("Deleting investment for id : {} ", id);
        Investment investment = investmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No investment Found for Id: "+ id));
        investmentRepository.delete(investment);
    }

    @Override
    public InvestmentDto updateCurrentPrice(Long id, BigDecimal currentPrice) {
        Investment investment = investmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No investment Found for Id: "+ id));
        investment.setCurrentPrice(currentPrice);
        Investment updated = investmentRepository.save(investment);
        return investmentMapper.toDto(updated);
    }
}
