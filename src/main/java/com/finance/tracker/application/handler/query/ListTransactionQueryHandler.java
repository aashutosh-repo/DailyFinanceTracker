package com.finance.tracker.application.handler.query;

import com.finance.tracker.application.ApplicationException;
import com.finance.tracker.application.dto.TransactionDTO;
import com.finance.tracker.application.handler.QueryHandler;
import com.finance.tracker.application.mapper.TransactionMapper;
import com.finance.tracker.application.query.GetTransactionQuery;
import com.finance.tracker.application.query.ListTransactionQuery;
import com.finance.tracker.application.query.ListTransactionQueryResult;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import com.finance.tracker.domain.transaction.FinancialTransactionRepository;
import com.finance.tracker.domain.transaction.TransactionId;
import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListTransactionQueryHandler implements QueryHandler<ListTransactionQuery, ListTransactionQueryResult> {

    private final FinancialTransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public ListTransactionQueryResult handle(ListTransactionQuery query) {
        validateCommand(query);

        LocalDate startDate = query.getStartDate();
        LocalDate endDate = query.getEndDate();
        int pageSize = query.getPageSize() > 0 ? query.getPageSize() : 20;
        int pageNumber = Math.max(0, query.getPageNumber());

        List<FinancialTransaction> transactions;

        if (query.getType() != null) {
            transactions = repository.findByUserAndTypeAndDateRange(query.getUserId(),
                    query.getType(), startDate, endDate);
        } else if (query.getCategoryId() != null) {
            transactions = repository.findByUserAndCategoryAndDateRange(query.getUserId(),
                    query.getCategoryId(), startDate, endDate);
        } else {
            transactions = repository.findByUserAndDateRange(query.getUserId(),
                    startDate, endDate);
        }
        long totalCount = transactions.size();

        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, transactions.size());
        List<TransactionDTO> pageTransactions = transactions.subList(
                Math.min(fromIndex, transactions.size()), toIndex)
                .stream()
                .map(mapper::toDTO)
                .toList();
        int totalPages = (int)Math.ceil((double) totalCount/pageSize);

        return ListTransactionQueryResult.builder()
                .transactions(pageTransactions)
                .totalCount(totalCount)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .totalPages(totalPages)
                .build();


    }

    private void validateCommand(ListTransactionQuery queryHandler) {
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
