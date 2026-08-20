package com.finance.tracker.domain.transaction.events;

import com.finance.tracker.domain.shared.DomainEvent;
import com.finance.tracker.domain.shared.Money;
import com.finance.tracker.domain.transaction.TransactionType;
import lombok.Getter;

import java.time.LocalDate;


@Getter
public class TransactionUpdatedEvent extends DomainEvent {

    private final Long userId;
    private final Money money;
    private final Money newAmount;
    private final String changeDescription;


    public TransactionUpdatedEvent(Long transactionId, Long userId, Money money,Money newAmount, String changeDescription) {
        super(transactionId);
        this.userId = userId;
        this.money = money;
        this.newAmount = newAmount;
        this.changeDescription = changeDescription;
    }
}
