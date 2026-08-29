package com.finance.tracker.stock.market.dto;


import lombok.Builder;
import java.util.UUID;

@Builder
public record MarketSyncJobResponse(
        UUID jobId,
        String status,
        String message
) { }