package com.finance.tracker.domain.transaction.events;

import com.finance.tracker.domain.shared.DomainEvent;
import com.finance.tracker.domain.shared.Money;
import com.finance.tracker.domain.transaction.TransactionType;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;


@Getter
public class TransactionCreatedEvent extends DomainEvent {

    private final Long userId;
    private final TransactionType type;
    private final Money money;
    private final LocalDate transactioDate;
    private final Long categoryId;
    private final String description;


    public TransactionCreatedEvent(Long transactionId, Long userId, TransactionType type, Money money, LocalDate transactioDate, Long categoryId, String description) {
        super(transactionId);
        this.userId = userId;
        this.type = type;
        this.money = money;
        this.transactioDate = transactioDate;
        this.categoryId = categoryId;
        this.description = description;
    }
}
