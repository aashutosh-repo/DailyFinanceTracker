package com.finance.tracker.domain.transaction.exceptions;

import com.finance.tracker.domain.shared.DomainException;

public class InvalidTransactionException extends DomainException {
    public InvalidTransactionException(String s) {
        super(s);
    }

    public InvalidTransactionException(String message , Throwable cause) {
        super(message, cause);
    }
}
