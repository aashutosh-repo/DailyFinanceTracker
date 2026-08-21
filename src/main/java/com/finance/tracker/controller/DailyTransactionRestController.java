package com.finance.tracker.controller;

import com.finance.tracker.application.command.*;
import com.finance.tracker.application.dto.TransactionDTO;
import com.finance.tracker.application.handler.command.*;
import com.finance.tracker.application.handler.query.GetFinancialSummeryQueryHandler;
import com.finance.tracker.application.handler.query.GetTransactionQueryHandler;
import com.finance.tracker.application.handler.query.ListTransactionQueryHandler;
import com.finance.tracker.application.query.*;
import com.finance.tracker.domain.transaction.TransactionType;
import com.finance.tracker.dto.CreateTransactionRequest;
import com.finance.tracker.dto.CreateTransferRequest;
import com.finance.tracker.dto.ListTransactionRequest;
import com.finance.tracker.dto.UpdateTransactionRequest;
import com.finance.tracker.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Get;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
    private final AuthService userDetailsService;

    @PostMapping("/expense")
    public ResponseEntity<TransactionDTO>  createExpense(
            @Valid @RequestBody CreateTransactionRequest request,
            @RequestParam(value = "userId", defaultValue = "1") String userId) {

        Long resolvedUserId = resolvedUserId(userId);
        CreateExpenseCommand command = CreateExpenseCommand.builder()
                .userId(resolvedUserId)
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

    @PostMapping("/income")
    public ResponseEntity<TransactionDTO>  createIncome(
            @Valid @RequestBody CreateIncomeCommand request,
            @RequestParam(value = "userId", defaultValue = "1") String userId) {

        Long resolvedUserId = resolvedUserId(userId);
        CreateIncomeCommand command = CreateIncomeCommand.builder()
                .userId(resolvedUserId)
                .amount(request.getAmount().toString())
                .currency(request.getCurrency())
                .transactionDate(request.getTransactionDate())
                .categoryId(request.getCategoryId())
                .description(request.getDescription())
                .incomeSource(request.getIncomeSource())
                .createdBy(request.getCreatedBy())
                .build();

        TransactionDTO response = incomeCommandHandler.handle(command);
        log.info("Income created successfully for user :  {}", userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO>  createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            @RequestParam(value = "userId", defaultValue = "1") String userId) {

        Long resolvedUserId = resolvedUserId(userId);
        CreateTransferCommand command = CreateTransferCommand.builder()
                .userId(resolvedUserId)
                .amount(request.getAmount().toString())
                .currency(request.getCurrency())
                .transactionDate(request.getTransactionDate())
                .sourceAccount(request.getSourceAccountId())
                .description(request.getDescription())
                .destinationAccount(request.getDestinationAccountId())
                .createdBy(request.getCreatedBy())
                .build();

        TransactionDTO response = transferCommandHandler.handle(command);
        log.info("Transfer created successfully for user :  {}", userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO>  getTransaction(
            @PathVariable Long id,
            @RequestParam(value = "userId", defaultValue = "1") String userId) {

        Long resolvedUserId = resolvedUserId(userId);

        GetTransactionQuery query = GetTransactionQuery.builder()
                .transactionId(id)
                .userId(resolvedUserId)
                .build();
        TransactionDTO dto = getTransactionQueryHandler.handle(query);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<ListTransactionQueryResult>  listTransactions(
            @Valid @ModelAttribute ListTransactionRequest request,
            @RequestParam(value = "userId", defaultValue = "1") String userId) {

        Long resolvedUserId = resolvedUserId(userId);

        TransactionType transactionType = null;
        if (request.getType() == null && !request.getType().isBlank()) {
            transactionType = TransactionType.valueOf(request.getType().toUpperCase());
        }

        ListTransactionQuery query = ListTransactionQuery.builder()
                .userId(resolvedUserId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(transactionType)
                .categoryId(request.getCategoryId())
                .pageSize(request.getPageSize())
                .pageNumber(request.getPageNumber())
                .sortBy(request.getSortBy())
                .sortOrder(request.getSortOrder())
                .build();
        ListTransactionQueryResult result  = listTransactionQueryHandler.handle(query);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}")
    public ResponseEntity<TransactionDTO>  updateTransactions(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateTransactionRequest request,
            @RequestParam(value = "userId", defaultValue = "1") String userId) {

        Long resolvedUserId = resolvedUserId(userId);

        UpdateTransactionCommand query = UpdateTransactionCommand.builder()
                .transactionId(id)
                .userId(resolvedUserId)
                .amount(request.getAmount() != null ? request.getAmount().toString() : null)
                .description(request.getDescription())
                .categoryId(request.getCategoryId())
                .paymentMethod(request.getPaymentMethod())
                .receiptUrl(request.getReceiptUrl())
                .incomeSource(request.getIncomeSource())
                .build();
        TransactionDTO result  = updateTransactionCommandHandler.handle(query);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteTransactions(
            @PathVariable Long id,
            @RequestParam(value = "reason", defaultValue = "user requested deletion") String reason,
            @RequestParam(value = "userId", defaultValue = "1") String userId,
            @RequestParam(value = "deletedBy", defaultValue = "admin") String deletedBy) {

        Long resolvedUserId = resolvedUserId(userId);

        DeleteTransactionCommand query = DeleteTransactionCommand.builder()
                .transactionId(id)
                .userId(resolvedUserId)
                .reason(reason)
                .deletedBy(deletedBy)
                .build();
       deleteTransactionCommandHandler.handle(query);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summery")
    public ResponseEntity<FinancialSummeryDTO> getFinancialSummery(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(value = "currency", defaultValue = "INR") String currency,
            @RequestParam(value = "userId", defaultValue = "1") String userId) {

        Long resolvedUserId = resolvedUserId(userId);
        GetFinancialSummeryQuery query = GetFinancialSummeryQuery.builder()
                .userId(resolvedUserId)
                .startDate(startDate)
                .endDate(endDate)
                .currency(currency)
                .build();
        FinancialSummeryDTO summeryDTO = getFinancialSummeryQueryHandler.handle(query);
        return ResponseEntity.ok(summeryDTO);
    }

    private Long resolvedUserId(String userId) {
        if (userId == null) {
            return 1L;
        }
        return userDetailsService.resolveDataBaseUserId(userId);
    }
}
