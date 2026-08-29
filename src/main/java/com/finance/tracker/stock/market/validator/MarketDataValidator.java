package com.finance.tracker.stock.market.validator;

import com.finance.tracker.stock.market.dto.MarketData;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class MarketDataValidator {

    public void validate(MarketData data) {

        if (data.priceDate() == null) {
            throw new IllegalArgumentException("Price date cannot be null");
        }

        if (data.open() == null || data.high() == null ||
                data.low() == null || data.close() == null) {

            throw new IllegalArgumentException("OHLC values cannot be null");
        }

        if (data.volume() == null || data.volume() < 0) {

            throw new IllegalArgumentException("Volume cannot be negative");
        }

        validatePositive(data.open(), "Open");
        validatePositive(data.high(), "High");
        validatePositive(data.low(), "Low");
        validatePositive(data.close(), "Close");
        validateHighLow(data);
    }

    private void validatePositive(BigDecimal value, String field) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(field + " price must be greater than zero");
        }
    }

    private void validateHighLow(MarketData data) {

        BigDecimal highest = data.open()
                        .max(data.close());

        BigDecimal lowest = data.open()
                        .min(data.close());

        if (data.high().compareTo(highest) < 0) {
            throw new IllegalArgumentException("High price is invalid");
        }

        if (data.low().compareTo(lowest) > 0) {
            throw new IllegalArgumentException("Low price is invalid");
        }
    }
}
