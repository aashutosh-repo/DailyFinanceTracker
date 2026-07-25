package com.finance.tracker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Cache Service
 * Handles caching operations for frequently accessed data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    /**
     * Cache key prefix for user expenses
     */
    private static final String EXPENSE_CACHE_PREFIX = "expenses:";
    
    /**
     * Cache key prefix for user budgets
     */
    private static final String BUDGET_CACHE_PREFIX = "budgets:";
    
    /**
     * Cache key prefix for user income
     */
    private static final String INCOME_CACHE_PREFIX = "income:";
    
    /**
     * Get expense cache key
     */
    public String getExpenseCacheKey(Long userId) {
        return EXPENSE_CACHE_PREFIX + userId;
    }
    
    /**
     * Get budget cache key
     */
    public String getBudgetCacheKey(Long userId) {
        return BUDGET_CACHE_PREFIX + userId;
    }
    
    /**
     * Get income cache key
     */
    public String getIncomeCacheKey(Long userId) {
        return INCOME_CACHE_PREFIX + userId;
    }
    
    /**
     * Invalidate all caches for a user
     */
    public void invalidateUserCaches(Long userId) {
        log.debug("Invalidating all caches for user: {}", userId);
        // Caching framework will handle this with @CacheEvict annotations
    }
    
    /**
     * Invalidate expense cache for a user
     */
    public void invalidateUserExpenses(String userId) {
        log.debug("Invalidating expense cache for user: {}", userId);
    }
    
    /**
     * Check if cache is enabled
     */
    public boolean isCacheEnabled() {
        return true; // Can be made configurable
    }
}
