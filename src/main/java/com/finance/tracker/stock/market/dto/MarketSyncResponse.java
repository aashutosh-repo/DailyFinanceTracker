package com.finance.tracker.stock.market.dto;

import lombok.Builder;

@Builder
public record MarketSyncResponse(
        String symbol,
        int totalRecords,
        int insertedRecords,
        int updatedRecords,
        String provider
) { }