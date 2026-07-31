package com.finance.tracker.chatbot.indexing;

import com.finance.tracker.events.ChangeType;

import java.time.YearMonth;

public interface FinancialIndexingService {

    void indexMonthlySummary(String userId, YearMonth month);
    void indexBudgetStatus(String userId, YearMonth month);
    void reindexUser(String userId);
    void indexIncomeSummery(String userId, YearMonth month);
    void indexExpenseSummery(String userId, YearMonth month);
    void indexByChangeType(String userId, YearMonth month, ChangeType changeType);

}