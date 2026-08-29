package com.finance.tracker.stock.market.util;

import com.finance.tracker.stock.market.service.SyncJobService;
import com.finance.tracker.stock.market.service.MarketPriceSyncService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceSyncWorker {

    private final SyncJobService syncJobService;
    private final MarketPriceSyncService marketPriceSyncService;


    @Async("marketSyncExecutor")
    public void process(UUID jobId) {

        try {
            log.info("Starting market price sync. jobId={}", jobId);
            // Mark RUNNING
            syncJobService.markRunning(jobId);

            // Execute sync business logic
            marketPriceSyncService.executeSync(jobId);
            log.info("Market price sync completed. jobId={}", jobId);

        } catch (Exception exception) {
            log.error("Market price sync failed. jobId={}", jobId, exception);
            syncJobService.markFailed(jobId, exception);
        }
    }
}