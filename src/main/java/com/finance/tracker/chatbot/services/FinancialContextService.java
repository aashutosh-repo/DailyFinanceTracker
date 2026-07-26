package com.finance.tracker.chatbot.services;

import com.finance.tracker.chatbot.rag.context.FinancialContext;

import java.time.YearMonth;

public interface FinancialContextService {
    FinancialContext getMonthlyContext(Long userId, YearMonth month);
}
