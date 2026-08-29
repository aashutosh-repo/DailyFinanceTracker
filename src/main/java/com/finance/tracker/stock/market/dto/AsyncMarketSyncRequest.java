package com.finance.tracker.stock.market.dto;

import java.time.LocalDate;

public record AsyncMarketSyncRequest(
        LocalDate from,
        LocalDate to
) {
}