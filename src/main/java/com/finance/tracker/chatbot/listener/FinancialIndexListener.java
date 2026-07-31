package com.finance.tracker.chatbot.listener;

import com.finance.tracker.chatbot.indexing.FinancialIndexingService;
import com.finance.tracker.events.FinancialDataChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancialIndexListener {

    private static final long DEBOUNCE_MS = 5_000;
    private final FinancialIndexingService indexingService;
    private final ConcurrentHashMap<String, Long> lastIndexedAt = new ConcurrentHashMap<>();

    @Async("indexingExecuter")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFinancialDataChanged(FinancialDataChangedEvent event) {

        log.info("Reindexing financial data for user {} month {}", event.getUserId(), event.getMonth());

        String debounceKey = event.getUserId() + "_" +
                event.getMonth() + "_" +
                event.getChangeType();

        long now = System.currentTimeMillis();
        Long lastRun = lastIndexedAt.get(debounceKey);
        if(lastRun != null && (now - lastRun) < DEBOUNCE_MS) {
            log.debug("Debounced index request for Key= {} ({}ms since last run)", debounceKey, now-lastRun);
            return;
        }
        lastIndexedAt.put(debounceKey, now);
        log.info("[VectorIndex] received changeType {} for UserId {}", event.getChangeType(), event.getUserId());

        try {
            indexingService.indexByChangeType(
                    event.getUserId(),
                    event.getMonth(),
                    event.getChangeType());
        } catch (Exception e ){
            log.error("[VectorIndexing] failed for userId = {}", event.getUserId());
        }
    }
}