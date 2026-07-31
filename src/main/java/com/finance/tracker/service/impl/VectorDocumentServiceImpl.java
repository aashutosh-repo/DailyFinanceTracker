package com.finance.tracker.service.impl;

import com.finance.tracker.chatbot.rag.document.DocumentType;
import com.finance.tracker.service.VectorDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorDocumentServiceImpl implements VectorDocumentService {

    private final JdbcTemplate vectorJdbcTemplate;

    @Override
    public void deleteMonthlySummary(
            String userId,
            YearMonth month) {
        deleteByDocumentType(userId, month, DocumentType.MONTHLY_SUMMARY);
    }

    @Override
    public void deleteAllForUser(String userId) {
        int rows = vectorJdbcTemplate.update("""
                DELETE FROM vector_store
                WHERE metadata->>'userId' = ?
                """, userId);
        log.info("Deleted {} vector documents for userId {}", rows, userId);
    }

    @Override
    public void deleteByDocumentType(String userId, YearMonth month, DocumentType type) {
        int rows = vectorJdbcTemplate.update("""
            DELETE FROM vector_store
            WHERE metadata->>'userId' = ?
              AND metadata->>'month' = ?
              AND metadata->>'documentType' = ?
            """, userId, month.toString(), DocumentType.MONTHLY_SUMMARY);
        log.info("Deleted {} vector documents for userId {} month {} type {}", rows, userId, month, type);
    }
}