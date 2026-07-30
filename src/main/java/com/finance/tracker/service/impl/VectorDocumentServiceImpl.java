package com.finance.tracker.service.impl;

import com.finance.tracker.chatbot.rag.document.DocumentType;
import com.finance.tracker.service.VectorDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class VectorDocumentServiceImpl implements VectorDocumentService {

    private final JdbcTemplate vectorJdbcTemplate;

    @Override
    public void deleteMonthlySummary(
            String userId,
            YearMonth month) {

        vectorJdbcTemplate.update("""
            DELETE FROM vector_store
            WHERE metadata->>'userId' = ?
              AND metadata->>'month' = ?
              AND metadata->>'documentType' = ?
            """,
                userId,
                month.toString(),
                DocumentType.MONTHLY_SUMMARY.name());
    }
}