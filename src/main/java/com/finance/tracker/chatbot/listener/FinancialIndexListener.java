package com.finance.tracker.chatbot.listener;

import com.finance.tracker.chatbot.indexing.FinancialIndexingService;
import com.finance.tracker.events.FinancialDataChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancialIndexListener {

//    private static final long DEBOUNCE_MS = 5_000;
    private final FinancialIndexingService indexingService;
    private final ConcurrentHashMap<String, Long> lastIndexedAt = new ConcurrentHashMap<>();

    @Async("indexingExecuter")
    @CacheEvict(value = "financialContext", key= "#event.userId + '_' +#event.month")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true)
    public void handleFinancialDataChanged(FinancialDataChangedEvent event) {

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