package com.finance.tracker.application.mapper;

import com.finance.tracker.application.dto.TransactionDTO;
import com.finance.tracker.domain.shared.Money;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public TransactionDTO toDTO(FinancialTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        Money money = transaction.getMoney();
        String amountStr = money != null ? money.getAmount().toPlainString() : null;
        String currencyStr = money != null ? money.getCurrency() : null;

        return TransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .amount(amountStr)
                .currency(currencyStr)
                .transactionDate(transaction.getTransactionDate())
                .categoryId(transaction.getCategoryId())
                .description(transaction.getDescription())
                .createAt(transaction.getCreatedAt())
                .updateAt(transaction.getUpdatedAt())
                .createBy(transaction.getCreatedBy())
                .updateBy(transaction.getUpdatedBy())
                .deleteAt(transaction.getDeletedAt())
                .incomeSource(transaction.getIncomeSource())
                .paymentMethod(transaction.getPaymentMethod())
                .receiptUrl(transaction.getReceiptUrl())
                .tags(transaction.getTags())
                .sourceAccountId(transaction.getSourceAccountId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .investmentId(transaction.getInvestmentId())
                .investmentType(transaction.getInvestmentType())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice() != null ? transaction.getPrice(): null)
                .build();
    }
}
