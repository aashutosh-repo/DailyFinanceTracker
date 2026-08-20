package com.finance.tracker.domain.transaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class TransactionId {
    private final Long value;

    public static TransactionId of(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Please enter a valid transaction id" + id);
        }
        return  new TransactionId(id);
    }
}
