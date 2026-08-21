package com.finance.tracker.service.impl;

import com.finance.tracker.domain.transaction.*;
import com.finance.tracker.entity.FinancialTransactionEntity;
import com.finance.tracker.mapper.FinancialTransactionMapper;
import com.finance.tracker.repository.FinancialTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FinancialTransactionServiceImpl implements FinancialTransactionRepository {

    private final FinancialTransactionJpaRepository jpaRepository;
    private final FinancialTransactionMapper transactionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public FinancialTransaction save(FinancialTransaction transaction) {
        FinancialTransactionEntity entity = transactionMapper.toEntity(transaction);

        FinancialTransactionEntity savedEntity = jpaRepository.save(entity);

        FinancialTransaction savedDomain = transactionMapper.toDomain(savedEntity);

        for (Object event : transaction.getDomainEventAndClear()) {
            eventPublisher.publishEvent(event);
        }
        return savedDomain;
    }

    @Override
    public Optional<FinancialTransaction> findById(TransactionId id) {
        return jpaRepository.findById(id.getValue()).map(transactionMapper::toDomain);
    }

    @Override
    public List<FinancialTransaction> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByUserIdAndDateRange(userId, startDate, endDate)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<FinancialTransaction> findActiveByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findActiveByUserIdAndDateRange(userId, TransactionStatus.POSTED, startDate, endDate)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<FinancialTransaction> findByUserAndTypeAndDateRange(Long userId, TransactionType type, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByUserIdAndTypeAndDateRange(userId, type, startDate, endDate)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<FinancialTransaction> findByUserAndCategoryAndDateRange(Long userId, Long categoryId, LocalDate startDate, LocalDate enddate) {
        return jpaRepository.findByUserIdAndCategoryAndDateRange(userId, categoryId, startDate, enddate)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(FinancialTransaction entity) {
        save(entity);
    }

    @Override
    public boolean existById(TransactionId id) {
        return jpaRepository.existsById(id.getValue());
    }
}
