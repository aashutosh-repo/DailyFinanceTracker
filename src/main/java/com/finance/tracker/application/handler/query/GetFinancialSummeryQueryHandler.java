package com.finance.tracker.application.handler.query;

import com.finance.tracker.application.handler.QueryHandler;
import com.finance.tracker.application.mapper.TransactionMapper;
import com.finance.tracker.application.query.FinancialSummeryDTO;
import com.finance.tracker.application.query.GetFinancialSummeryQuery;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import com.finance.tracker.domain.transaction.FinancialTransactionRepository;
import com.finance.tracker.domain.transaction.TransactionStatus;
import com.finance.tracker.domain.transaction.TransactionType;
import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetFinancialSummeryQueryHandler implements QueryHandler<GetFinancialSummeryQuery, FinancialSummeryDTO> {

    private final FinancialTransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public FinancialSummeryDTO handle(GetFinancialSummeryQuery query) {
        validateCommand(query);

        //Load all transaction
        List<FinancialTransaction> allTransaction = repository.findActiveByUserAndDateRange(query.getUserId(),
                query.getStartDate(), query.getEndDate());

        //Only POSTED
        List<FinancialTransaction> postedTransaction = allTransaction.stream()
                .filter(tx -> tx.getStatus() == TransactionStatus.POSTED)
                .toList();

        BigDecimal totalIncomeAmount = BigDecimal.ZERO;
        BigDecimal totalExpenseAmount = BigDecimal.ZERO;
        long incomeCount = 0;
        long expenseCount = 0;
        long transferCount = 0;

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();

        for (FinancialTransaction transaction : postedTransaction) {
            BigDecimal amount = transaction.getMoney().getAmount();

            if(transaction.getType() == TransactionType.INCOME) {
                totalIncomeAmount = totalIncomeAmount.add(amount);
                incomeCount++;
                String categoryKey = "Category_"+transaction.getCategoryId();
                incomeByCategory.put(categoryKey, incomeByCategory.getOrDefault(categoryKey, BigDecimal.ZERO).add(amount));
            }else if (transaction.getType() == TransactionType.EXPENSE) {
                totalExpenseAmount = totalIncomeAmount.add(amount);
                expenseCount++;
                String categoryKey = "Category_"+transaction.getCategoryId();
                expenseByCategory.put(categoryKey, expenseByCategory.getOrDefault(categoryKey, BigDecimal.ZERO).add(amount));
            }else if (transaction.getType() == TransactionType.TRANSFER) {
                transferCount ++;
            }
        }

        BigDecimal net = totalIncomeAmount.subtract(totalExpenseAmount);

        return FinancialSummeryDTO.builder()
                .currency(query.getCurrency())
                .totalExpense(totalExpenseAmount.toPlainString())
                .totalIncome(totalIncomeAmount.toPlainString())
                .net(net.toPlainString())
                .incomeTransactionCount(incomeCount)
                .expenseTransactionCount(expenseCount)
                .transferCount(transferCount)
                .incomeByCategory(incomeByCategory.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().toPlainString())))
                .expenseByCategory(expenseByCategory.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().toPlainString())))
                .build();


    }

    private void validateCommand(GetFinancialSummeryQuery queryHandler) {
        try {
            if (queryHandler.getUserId() == null || queryHandler.getUserId() <= 0) {

                throw new InvalidTransactionException("UserId must not be Null");
            }
            if (queryHandler.getStartDate() == null ) {
                throw new InvalidTransactionException("start date is Required");
            }
            if (queryHandler.getEndDate() == null ) {
                throw new InvalidTransactionException("End date is Required");
            }
            if (queryHandler.getStartDate().isAfter(queryHandler.getEndDate())) {
                throw new InvalidTransactionException("start date must be before end Date");
            }

        } catch(InvalidTransactionException e){
            throw new RuntimeException(e);
        }
    }
}
