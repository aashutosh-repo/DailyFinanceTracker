package com.finance.tracker.stock.market.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SyncJobResponse(
        UUID jobId,
        String symbol,
        String status,
        LocalDate fromDate,
        LocalDate toDate,
        String provider,
        Integer totalRecords,
        Integer insertedRecords,
        Integer updatedRecords,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}