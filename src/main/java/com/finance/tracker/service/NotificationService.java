package com.finance.tracker.service;

import com.finance.tracker.dto.NotificationDto;
import com.finance.tracker.entity.Budget;
import com.finance.tracker.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface NotificationService {

    void notifyExpenseCreated(User user, String description, String amount);
    void sendBudgetExceededAlert(String userId, Budget budget, BigDecimal totalSpent);
    void sendBudgetThresholdAlert(String userId, Budget budget, BigDecimal totalSpent);
    void notifyBudgetThreshold(User user, String budgetName, Double percentageUsed);
    void notifyIncomeAdded(User user, String source, String amount);
    void sendImportCompleteNotification(String userId, int importedCount, int failedCount);
    void markAllAsRead(String userId);
    NotificationDto markAsRead(Long id);
    List<NotificationDto> getUserNotification(String userId);
    void deleteNotification(Long id);
    Long getUnreadCount(String userId);
}