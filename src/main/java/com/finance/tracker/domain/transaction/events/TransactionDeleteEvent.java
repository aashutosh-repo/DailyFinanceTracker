package com.finance.tracker.domain.transaction.events;

import com.finance.tracker.domain.shared.DomainEvent;
import com.finance.tracker.domain.shared.Money;
import lombok.Getter;


@Getter
public class TransactionDeleteEvent extends DomainEvent {

    private final Long userId;
    private final Money money;
    private final String reason;


    public TransactionDeleteEvent(Long transactionId, Long userId, Money money, String reason) {
        super(transactionId);
        this.userId = userId;
        this.money = money;
        this.reason= reason;
    }
}
