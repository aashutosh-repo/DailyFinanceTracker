package com.finance.tracker.application.handler.command;

import com.finance.tracker.application.ApplicationException;
import com.finance.tracker.application.command.CreateExpenseCommand;
import com.finance.tracker.application.command.UpdateTransactionCommand;
import com.finance.tracker.application.dto.TransactionDTO;
import com.finance.tracker.application.handler.CommandHandler;
import com.finance.tracker.application.mapper.TransactionMapper;
import com.finance.tracker.domain.shared.Money;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import com.finance.tracker.domain.transaction.FinancialTransactionRepository;
import com.finance.tracker.domain.transaction.TransactionId;
import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateTransactionCommandHandler implements CommandHandler<UpdateTransactionCommand, TransactionDTO> {

    private final FinancialTransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public TransactionDTO handle(UpdateTransactionCommand command) {
        validateCommand(command);

        FinancialTransaction transaction = repository.findById(new TransactionId(command.getTransactionId()))
                .orElseThrow(() -> new ApplicationException("Transaction Not Found for Id: " + command.getTransactionId()));

        if (!transaction.getUserId().equals(command.getUserId())) {
            throw new ApplicationException("Unauthorized: invalid access");
        }

        if (command.getAmount() != null && !command.getAmount().isEmpty() ) {
            Money money = Money.of(command.getAmount(),transaction.getMoney().getCurrency());
            transaction.updateAmount(money);
        }

        if (command.getTransactionDate() != null) {
            transaction.updateTransactionDate(command.getTransactionDate());
        }

        if (command.getDescription() != null && !command.getDescription().isEmpty() ) {
            transaction.updateDescription(command.getDescription());
        }

        if (command.getCategoryId() != null && command.getCategoryId() > 0 ) {
            transaction.updateCategory(command.getCategoryId());
        }

        if (command.getPaymentMethod() != null && command.getPaymentMethod().isEmpty() ) {
            transaction.updatePaymentMethod(command.getPaymentMethod());
        }
       if (command.getReceiptUrl() != null && command.getReceiptUrl().isEmpty() ) {
            transaction.updateReceiptUrl(command.getReceiptUrl());
        }
       if (command.getIncomeSource() != null && command.getIncomeSource().isEmpty() ) {
            transaction.updateIncomeSource(command.getIncomeSource());
        }

        transaction = repository.save(transaction);
        return mapper.toDTO(transaction);
    }

    private void validateCommand(UpdateTransactionCommand command) {
        try {
            if (command.getUserId() == null || command.getUserId() <= 0) {

                throw new InvalidTransactionException("UserId must not be Null");
            }
            if (command.getTransactionId() == null || command.getTransactionId() <= 0) {
                throw new InvalidTransactionException("TransactionId must be valid");
            }
        } catch(InvalidTransactionException e){
            throw new RuntimeException(e);
        }
    }
}
