package com.finance.tracker.controller;

import com.finance.tracker.application.command.CreateExpenseCommand;
import com.finance.tracker.application.dto.TransactionDTO;
import com.finance.tracker.application.handler.command.*;
import com.finance.tracker.application.handler.query.GetFinancialSummeryQueryHandler;
import com.finance.tracker.application.handler.query.GetTransactionQueryHandler;
import com.finance.tracker.application.handler.query.ListTransactionQueryHandler;
import com.finance.tracker.dto.CreateTransactionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/transactions")
public class DailyTransactionRestController {

    private final CreateExpenseCommandHandler expenseCommandHandler;
    private final CreateTransferCommandHandler transferCommandHandler;
    private final CreateIncomeCommandHandler incomeCommandHandler;
    private final UpdateTransactionCommandHandler updateTransactionCommandHandler;
    private final DeleteTransactionCommandHandler deleteTransactionCommandHandler;

    private final GetTransactionQueryHandler getTransactionQueryHandler;
    private final ListTransactionQueryHandler listTransactionQueryHandler;
    private final GetFinancialSummeryQueryHandler getFinancialSummeryQueryHandler;

    @PostMapping("/expense")
    public ResponseEntity<TransactionDTO>  createExpense(
            @Valid @RequestBody CreateTransactionRequest request,
            @RequestParam(value = "userId", defaultValue = "1") Long userId) {

        CreateExpenseCommand command = CreateExpenseCommand.builder()
                .userId(userId)
                .amount(request.getAmount().toString())
                .currency(request.getCurrency())
                .transactionDate(request.getTransactionDate())
                .categoryId(request.getCategoryId())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod())
                .receiptUrl(request.getReceiptUrl())
                .createdBy(request.getCreatedBy())
                .build();

        TransactionDTO response = expenseCommandHandler.handle(command);
        log.info("Expense created successfully for user :  {}", userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
