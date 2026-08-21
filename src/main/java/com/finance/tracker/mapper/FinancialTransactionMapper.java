package com.finance.tracker.mapper;

import com.finance.tracker.domain.shared.Money;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import com.finance.tracker.entity.FinancialTransactionEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FinancialTransactionMapper {
    public FinancialTransaction toDomain(FinancialTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return FinancialTransaction.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .status(entity.getStatus())
                .money(Money.of(entity.getAmount(), entity.getCurrency()))
                .transactionDate(entity.getTransactionDate())
                .categoryId(entity.getCategoryId())
                .description(entity.getDescription())
                .incomeSource(entity.getIncomeSource())
                .paymentMethod(entity.getPaymentMethod())
                .receiptUrl(entity.getReceiptUrl())
                .tags(entity.getTags())
                .sourceAccountId(entity.getSourceAccountId())
                .destinationAccountId(entity.getDestinationAccountId())
                .investmentType(entity.getInvestmentType())
                .investmentId(entity.getInvestmentId())
                .quantity(entity.getQuantity())
                .price(toString(entity.getPrice()))
                .build();
    }

    public FinancialTransactionEntity toEntity(FinancialTransaction domain) {
        if (domain == null) {
            return null;
        }

        Money money = domain.getMoney();
        return FinancialTransactionEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .type(domain.getType())
                .status(domain.getStatus())
                .amount(toAmount(domain.getMoney()))
                .currency(toCurrency(domain.getMoney()))
                .transactionDate(domain.getTransactionDate())
                .categoryId(domain.getCategoryId())
                .description(domain.getDescription())
                .incomeSource(domain.getIncomeSource())
                .paymentMethod(domain.getPaymentMethod())
                .receiptUrl(domain.getReceiptUrl())
                .tags(domain.getTags())
                .sourceAccountId(domain.getSourceAccountId())
                .destinationAccountId(domain.getDestinationAccountId())
                .investmentType(domain.getInvestmentType())
                .investmentId(domain.getInvestmentId())
                .quantity(domain.getQuantity())
                .price(toBigDecimal(domain.getPrice()))
                .build();
    }

    private BigDecimal toAmount(Money money) {
        return money != null ? money.getAmount() : null;
    }

    private String toCurrency(Money money) {
        return money != null ? money.getCurrency() : null;
    }

    private String toString(BigDecimal value) {
        return value != null ? value.toPlainString() : null;
    }

    private BigDecimal toBigDecimal(String value) {
        return value != null ? new BigDecimal(value) : null;
    }
}
