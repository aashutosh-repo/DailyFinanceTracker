package com.finance.tracker.domain.transaction.exceptions;

import com.finance.tracker.domain.shared.DomainException;

public class InvalidAmountException extends DomainException {
    public InvalidAmountException(String s) {
        super(s);
    }

    public InvalidAmountException(String message , Throwable cause) {
        super(message, cause);
    }
}
