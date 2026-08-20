package com.finance.tracker.domain.transaction.exceptions;

import com.finance.tracker.domain.shared.DomainException;
import com.finance.tracker.domain.transaction.TransactionType;

public class InvalidCategoryForTypeException extends DomainException {
    public InvalidCategoryForTypeException(Long categoryId, TransactionType type) {
        super(String.format("category %d doesnot support transaction type %s", categoryId, type));
    }

    public InvalidCategoryForTypeException(String message , Throwable cause) {
        super(message, cause);
    }
}
