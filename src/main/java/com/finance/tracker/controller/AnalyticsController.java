package com.finance.tracker.controller;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.services.FinancialAnalyticsService;
import com.finance.tracker.service.impl.FinancialTransactionReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {

    private final FinancialAnalyticsService analyticsService;

    @GetMapping("/monthly")
    public ResponseEntity<FinancialContext> getMonthlyContext(
            @RequestParam String userId,
            @RequestParam(required = false) String month // expected format: YYYY-MM
    ) {
        YearMonth ym;
        try {
            ym = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        FinancialContext context = analyticsService.getMonthlyContext(userId, ym);
        return ResponseEntity.ok(context);
    }
}
