package com.finance.tracker.chatbot.listener;

import com.finance.tracker.chatbot.indexing.FinancialIndexingService;
import com.finance.tracker.events.FinancialDataChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancialIndexListener {

    private final FinancialIndexingService indexingService;

    @EventListener
    public void handleFinancialDataChanged(
            FinancialDataChangedEvent event) {

        log.info(
                "Reindexing financial data for user {} month {}",
                event.getUserId(),
                event.getMonth());

        indexingService.indexMonthlySummary(
                event.getUserId(),
                event.getMonth());
    }
}