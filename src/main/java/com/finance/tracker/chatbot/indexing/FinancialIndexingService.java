package com.finance.tracker.chatbot.indexing;

import java.time.YearMonth;

public interface FinancialIndexingService {

    void indexMonthlySummary(String userId, YearMonth month);
    void reindexUser(String userId);
    private void deleteExistingSummary(String userId, YearMonth month) {
        // TODO
    }
}