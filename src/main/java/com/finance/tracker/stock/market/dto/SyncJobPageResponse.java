package com.finance.tracker.stock.market.dto;

import java.util.List;

public record SyncJobPageResponse(

        List<SyncJobResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
