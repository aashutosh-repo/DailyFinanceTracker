package com.finance.tracker.stock.market.dto;

import java.util.UUID;

public record AsyncMarketSyncResponse(
        UUID jobId,
        String status
) {
}
