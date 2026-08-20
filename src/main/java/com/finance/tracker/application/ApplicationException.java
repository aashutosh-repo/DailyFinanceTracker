package com.finance.tracker.application;

import com.finance.tracker.domain.transaction.TransactionType;

public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message , Throwable cause) {
        super(message, cause);
    }
}
