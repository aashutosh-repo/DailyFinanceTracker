package com.finance.tracker.domain.transaction;

import com.finance.tracker.domain.shared.Money;
import com.finance.tracker.domain.transaction.events.TransactionCreatedEvent;
import com.finance.tracker.domain.transaction.events.TransactionDeleteEvent;
import com.finance.tracker.domain.transaction.events.TransactionUpdatedEvent;
import com.finance.tracker.domain.transaction.exceptions.InvalidAmountException;
import jakarta.transaction.InvalidTransactionException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public class FinancialTransaction {

    private Long id;
    private Long userId;
    private TransactionType type;
    private TransactionStatus status;
    private Money money;
    private LocalDate transactionDate;
    private Long categoryId;
    private String description;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private String updatedBy;
    private String createdBy;
    private LocalDate deletedAt;
    private String incomeSource;

    private String paymentMethod;
    private  String receiptUrl;
    private Set<String> tags;

    private Long sourceAccountId;
    private Long destinationAccountId;

    private String investmentType;
    private Long investmentId;
    private BigDecimal quantity;
    private String price;

    @Getter(AccessLevel.PACKAGE)
    @Builder.Default
    private List<Object> domainEvents = new ArrayList<>();


    //FactoryMethods = transaction of specific Types

    public static FinancialTransaction createExpense(Long userId, Money money, LocalDate transactionDate,
                                                     Long categoryId, String description, String paymentMethod, String createdBy) {
        validateBasicFields(userId,money, transactionDate, categoryId);
        FinancialTransaction expense = FinancialTransaction.builder()
                .userId(userId)
                .type(TransactionType.EXPENSE)
                .status(TransactionStatus.POSTED)
                .money(money)
                .transactionDate(transactionDate)
                .categoryId(categoryId)
                .description(description)
                .paymentMethod(paymentMethod)
                .tags(new HashSet<>())
                .updatedAt(LocalDate.now())
                .createdAt(LocalDate.now())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .domainEvents(new ArrayList<>())
                .build();

        expense.domainEvents.add(new TransactionCreatedEvent(
                null, userId, TransactionType.EXPENSE, money, transactionDate, categoryId, description
        ));
        return expense;
    }

    public static FinancialTransaction createIncome(Long userId, Money money, LocalDate transactionDate,
                                                     Long categoryId, String description, String incomeSource, String createdBy) {
        validateBasicFields(userId,money, transactionDate, categoryId);
        FinancialTransaction expense = FinancialTransaction.builder()
                .userId(userId)
                .type(TransactionType.INCOME)
                .status(TransactionStatus.POSTED)
                .money(money)
                .transactionDate(transactionDate)
                .categoryId(categoryId)
                .description(description)
                .incomeSource(incomeSource != null ? incomeSource : "OTHER")
                .updatedAt(LocalDate.now())
                .createdAt(LocalDate.now())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .domainEvents(new ArrayList<>())
                .build();

        expense.domainEvents.add(new TransactionCreatedEvent(
                null, userId, TransactionType.INCOME, money, transactionDate, categoryId, description
        ));
        return expense;
    }


    public static FinancialTransaction createTransfer(Long userId, Money money, LocalDate transactionDate,
                                                    Long sourceAccountId,Long destinationAccountId, String description, String createdBy) {
        validateBasicFields(userId,money, transactionDate, 1L);
        FinancialTransaction expense = FinancialTransaction.builder()
                .userId(userId)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.POSTED)
                .money(money)
                .transactionDate(transactionDate)
                .categoryId(null)
                .description(description)
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .updatedAt(LocalDate.now())
                .createdAt(LocalDate.now())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .domainEvents(new ArrayList<>())
                .build();

        expense.domainEvents.add(new TransactionCreatedEvent(
                null, userId, TransactionType.TRANSFER, money, transactionDate,null, description
        ));
        return expense;
    }

    public void updateAmount(Money newAmount) {
        try {
            if (!status.isMutable()) {
                throw new InvalidTransactionException("cannot update transaction in " + status + "status");
            }
            if (newAmount == null || !newAmount.isPositive()) {
                throw new InvalidAmountException("New Amount must be Positive: " + newAmount);
            }
        }catch (Exception e) {
            log.info("there is Some Error", e);
        }

        Money oldMoney = this.money;
        this.money = newAmount;
        this.updatedAt = LocalDate.now();

        //PublishEvent
        domainEvents.add(new TransactionUpdatedEvent(
                this.id, this.userId, oldMoney, newAmount, "Amount changed from " + oldMoney + " to " + newAmount
        ));
    }

    public void updateDescription(String newDescription) {
        try {
            if (!status.isMutable()) {
                throw new InvalidTransactionException("cannot update transaction in " + status + "status");
            }
        }catch (Exception e) {
            log.info("there is Some Error", e);
        }
        this.description = newDescription;
        this.updatedAt = LocalDate.now();

    }

    public void updateCategory(Long newCategoryId) {
        try {
            if (!status.isMutable()) {
                throw new InvalidTransactionException("cannot update transaction in " + status + "status");
            }

            if (type == TransactionType.TRANSFER) {
                throw new InvalidTransactionException("cannot change category of a TRANSFER");
            }
        }catch (Exception e) {
            log.info("there is Some Error", e);
        }

        this.categoryId = newCategoryId;
        this.updatedAt = LocalDate.now();

    }

    public void updateTransactionDate(LocalDate transactionDate) {
        try {
            if (!status.isMutable()) {
                throw new InvalidTransactionException("cannot update transaction in " + status + "status");
            }

            if (transactionDate == null) {
                throw new InvalidTransactionException("Transaction Date cannot be null");
            }
        }catch (Exception e) {
            log.info("there is Some Error", e);
        }

        this.transactionDate = transactionDate;
        this.updatedAt = LocalDate.now();

    }

    public void updatePaymentMethod(String paymentMethod) {
        try {
            if (!status.isMutable()) {
                throw new InvalidTransactionException("cannot update transaction in " + status + "status");
            }
            if (type == TransactionType.EXPENSE) {
                throw new InvalidTransactionException("Only Expense Category can be Updated");
            }
        }catch (Exception e) {
            log.info("there is Some Error", e);
        }
        this.paymentMethod = paymentMethod;
        this.updatedAt = LocalDate.now();

    }

    public void updateReceiptUrl(String newReceiptUrl) {
        try {
            if (!status.isMutable()) {
                throw new InvalidTransactionException("cannot update transaction in " + status + "status");
            }
            if (type == TransactionType.EXPENSE) {
                throw new InvalidTransactionException("Only Expense Category can be Updated");
            }
        }catch (Exception e) {
            log.info("there is Some Error", e);
        }
        this.receiptUrl = newReceiptUrl;
        this.updatedAt = LocalDate.now();

    }

    public void updateIncomeSource(String newIncomeSource) {
        try {
            if (!status.isMutable()) {
                throw new InvalidTransactionException("cannot update transaction in " + status + "status");
            }
            if (type == TransactionType.INCOME) {
                throw new InvalidTransactionException("Only INCOME Category can be Updated");
            }
        }catch (Exception e) {
            log.info("there is Some Error", e);
        }
        this.incomeSource = newIncomeSource;
        this.updatedAt = LocalDate.now();

    }

    public void addTags(String tag) {
        try {
            if (type != TransactionType.EXPENSE) {
                throw new InvalidTransactionException("Only Expense transaction can have tags");
            }

            if (tags == null) {
                new HashSet<>();
            }
        }catch (Exception e) {
            log.info("there is Some Error", e);
        }

        tags.add(tag);
        this.updatedAt = LocalDate.now();
    }

    public void removeTags(String tag) {
        if (tags != null) {
            tags.remove(tag);
            this.updatedAt = LocalDate.now();
        }
    }

    public void cancel(String reason) {
        try {
            if (type != TransactionType.EXPENSE) {
                throw new InvalidTransactionException("Only Expense transaction can have tags");
            }
        }catch (Exception e) {
            log.info("There is something Wrong");
        }

        this.status = TransactionStatus.CANCELLED;
        this.updatedAt = LocalDate.now();
        this.deletedAt = LocalDate.now();

        //publish Event
        domainEvents.add(new TransactionDeleteEvent(
                        this.id, this.userId, this.money, reason
        ));
    }

    public void archive() {
        try {
            if (status == TransactionStatus.ARCHIEVED || status == TransactionStatus.CANCELLED) {
                throw new InvalidTransactionException("Only Expense transaction can have tags");
            }
        }catch (Exception e) {
            log.info("There is something Wrong");
        }

        this.status = TransactionStatus.ARCHIEVED;
        this.updatedAt = LocalDate.now();
    }

    public List<Object> getDomainEventAndClear() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public Set<String> getTags() {
        if (tags == null){
          return Collections.emptySet();
        }
        return Collections.unmodifiableSet(tags);
    }

    private static void validateBasicFields(Long userId, Money money, LocalDate transactionDate, Long categoryId) {
        try {
            if (userId == null || userId == 0) {
                throw new InvalidTransactionException("UserId must not be Null");
            }
            if (money == null || !money.isPositive()) {
                throw new InvalidTransactionException("Amount must be Positive : " + money);
            }
            if (transactionDate == null) {
                throw new InvalidTransactionException("transaction date required");
            }
            if (categoryId == null || categoryId <= 0) {
                throw new InvalidTransactionException("category Id must be valid");
            }
        } catch(InvalidTransactionException e){
                throw new RuntimeException(e);
        }
    }
}
