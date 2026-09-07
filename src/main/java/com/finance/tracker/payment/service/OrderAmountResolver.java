package com.finance.tracker.payment.service;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderAmountResolver {
    Optional<BigDecimal> findAmount(String orderId, String customerId);
}
