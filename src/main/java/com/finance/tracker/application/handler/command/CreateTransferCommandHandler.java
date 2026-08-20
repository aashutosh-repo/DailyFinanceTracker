package com.finance.tracker.application.handler.command;

import com.finance.tracker.application.command.CreateIncomeCommand;
import com.finance.tracker.application.command.CreateTransferCommand;
import com.finance.tracker.application.dto.TransactionDTO;
import com.finance.tracker.application.handler.CommandHandler;
import com.finance.tracker.application.mapper.TransactionMapper;
import com.finance.tracker.domain.shared.Money;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import com.finance.tracker.domain.transaction.FinancialTransactionRepository;
import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateTransferCommandHandler implements CommandHandler<CreateTransferCommand, TransactionDTO> {

    private final FinancialTransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public TransactionDTO handle(CreateTransferCommand command) {
        validateCommand(command);

        Money money = Money.of(command.getAmount(),command.getCurrency());

        FinancialTransaction transfer = FinancialTransaction.createTransfer(
                command.getUserId(), money, command.getTransactionDate(),
                command.getSourceAccount(), command.getDestinationAccount(), command.getDescription(),
                command.getCreatedBy()
        );

        transfer = repository.save(transfer);
        return mapper.toDTO(transfer);
    }

    private void validateCommand(CreateTransferCommand command) {
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
            if (command.getCurrency() == null || command.getCurrency().isEmpty()) {
                throw new InvalidTransactionException("currency must required");
            }
            if (command.getSourceAccount() == null || command.getSourceAccount()<=0) {
                throw new InvalidTransactionException("source Account must required");
            }
            if (command.getDestinationAccount() == null || command.getDescription().isEmpty()) {
                throw new InvalidTransactionException("destination account must required");
            }
        } catch(InvalidTransactionException e){
            throw new RuntimeException(e);
        }
    }
}
