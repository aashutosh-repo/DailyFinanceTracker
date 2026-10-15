package com.finance.tracker.stock.market.validator;

import com.finance.tracker.stock.market.dto.MarketData;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class MarketDataValidator {

    public void validate(MarketData data) {

        if (data == null) {
            throw new IllegalArgumentException("Market data record cannot be null");
        }

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

        if (data.source() == null || data.source().isBlank()) {
            throw new IllegalArgumentException("Market data provider source cannot be blank");
        }

        validatePositive(data.open(), "Open");
        validatePositive(data.high(), "High");
        validatePositive(data.low(), "Low");
        validatePositive(data.close(), "Close");
        validateHighLow(data);
    }

    public void validateBatch(List<MarketData> records, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Requested market data date range is invalid");
        }
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("Provider returned no historical market data");
        }

        Set<LocalDate> seenDates = new HashSet<>();
        for (MarketData record : records) {
            validate(record);
            if (record.priceDate().isBefore(fromDate) || record.priceDate().isAfter(toDate)) {
                throw new IllegalArgumentException("Provider returned a price outside the requested date range");
            }
            if (!seenDates.add(record.priceDate())) {
                throw new IllegalArgumentException("Provider returned duplicate prices for " + record.priceDate());
            }
        }
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
