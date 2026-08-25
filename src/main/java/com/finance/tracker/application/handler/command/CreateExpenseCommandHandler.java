package com.finance.tracker.application.handler.command;

import com.finance.tracker.application.command.CreateExpenseCommand;
import com.finance.tracker.application.dto.TransactionDTO;
import com.finance.tracker.application.handler.CommandHandler;
import com.finance.tracker.application.mapper.TransactionMapper;
import com.finance.tracker.domain.shared.Money;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import com.finance.tracker.domain.transaction.FinancialTransactionRepository;
import jakarta.transaction.InvalidTransactionException;
import lombok.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateExpenseCommandHandler implements CommandHandler<CreateExpenseCommand, TransactionDTO> {

    private final FinancialTransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public TransactionDTO handle(CreateExpenseCommand command) {
        validateCommand(command);

        Money money = Money.of(command.getAmount(),command.getCurrency());
        FinancialTransaction expense = FinancialTransaction.createExpense(
                command.getUserId(),
                money,
                command.getTransactionDate(),
                command.getCategoryId(),
                command.getDescription(),
                command.getPaymentMethod(),
                command.getCreatedBy()
        );

        expense = repository.save(expense);

        return mapper.toDTO(expense);
    }

    private void validateCommand(CreateExpenseCommand command) {
        try {
            if (command.getUserId() == null || command.getUserId() <= 0) {

                throw new InvalidTransactionException("UserId must not be Null");
            }
            if (command.getAmount() == null || command.getAmount().isBlank()) {
                throw new InvalidTransactionException("Amount id Required");
            }
            if (command.getTransactionDate() == null) {
                throw new InvalidTransactionException("transaction date required");
            }
            if (command.getCategoryId() == null || command.getCategoryId() <= 0) {
                throw new InvalidTransactionException("category Id must be valid");
            }
        } catch(InvalidTransactionException e){
            throw new RuntimeException(e);
        }
    }
}
