package com.finance.tracker.application.handler.query;

import com.finance.tracker.application.ApplicationException;
import com.finance.tracker.application.dto.TransactionDTO;
import com.finance.tracker.application.handler.QueryHandler;
import com.finance.tracker.application.mapper.TransactionMapper;
import com.finance.tracker.application.query.GetTransactionQuery;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import com.finance.tracker.domain.transaction.FinancialTransactionRepository;
import com.finance.tracker.domain.transaction.TransactionId;
import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTransactionQueryHandler implements QueryHandler<GetTransactionQuery, TransactionDTO> {

    private final FinancialTransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public TransactionDTO handle(GetTransactionQuery query) {
        validateCommand(query);
        FinancialTransaction transaction = repository.findById(new TransactionId(query.getTransactionId()))
                .orElseThrow(() -> new ApplicationException("Transaction Not Found : "+ query.getTransactionId()));

        if (!transaction.getUserId().equals(query.getUserId())) {
            throw new ApplicationException("Unauthorized: invalid access");
        }
        return mapper.toDTO(transaction);
    }

    private void validateCommand(GetTransactionQuery command) {
        try {
            if (command.getUserId() == null || command.getUserId() <= 0) {

                throw new InvalidTransactionException("UserId must not be Null");
            }
            if (command.getTransactionId() == null || command.getTransactionId() <= 0) {
                throw new InvalidTransactionException("transaction id Required");
            }
        } catch(InvalidTransactionException e){
            throw new RuntimeException(e);
        }
    }
}
