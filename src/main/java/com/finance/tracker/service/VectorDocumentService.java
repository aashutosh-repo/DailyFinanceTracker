package com.finance.tracker.service;

import java.time.YearMonth;

public interface VectorDocumentService {

    void deleteMonthlySummary(String userId, YearMonth month);

}