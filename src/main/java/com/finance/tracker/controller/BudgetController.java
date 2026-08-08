package com.finance.tracker.controller;

import com.finance.tracker.dto.budget.BudgetRequest;
import com.finance.tracker.dto.budget.BudgetResponse;
import com.finance.tracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request,
            @RequestParam String userId) {
        try {
            BudgetResponse response = budgetService.createBudget(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create budget: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<BudgetResponse>> getBudget(@PathVariable String id) {
        try {
            List<BudgetResponse> responses = budgetService.getBudgetByUserId(id);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BudgetResponse>> getUserBudgets(@PathVariable String userId) {
        try {
            List<BudgetResponse> budgets = budgetService.getBudgetsByUser(userId);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        try {
            BudgetResponse response = budgetService.updateBudget(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        try {
            budgetService.deleteBudget(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
