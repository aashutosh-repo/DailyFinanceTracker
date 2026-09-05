package com.finance.tracker.stock.market.controller;

import com.finance.tracker.constants.SyncJobStatus;
import com.finance.tracker.stock.market.service.SyncJobService;
import com.finance.tracker.stock.market.dto.SyncJobPageResponse;
import com.finance.tracker.stock.market.dto.SyncJobResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stocks/sync-jobs")
@RequiredArgsConstructor
public class StockSyncJobController {

    private final SyncJobService syncJobService;


    @GetMapping("/{jobId}")
    public SyncJobResponse getSyncJob(
            @PathVariable UUID jobId
    ) {

        return syncJobService.getJob(jobId);
    }

    @GetMapping
    public SyncJobPageResponse getSyncJobs(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) SyncJobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startedAt,desc") String sort
    ) {
        return syncJobService.getJobs(
                symbol,
                status,
                page,
                size,
                sort
        );
    }
}