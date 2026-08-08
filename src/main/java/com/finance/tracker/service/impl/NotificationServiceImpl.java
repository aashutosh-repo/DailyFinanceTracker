package com.finance.tracker.service.impl;

import com.finance.tracker.dto.NotificationDto;
import com.finance.tracker.entity.Budget;
import com.finance.tracker.entity.Notification;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.mapper.NotificationMapper;
import com.finance.tracker.repository.NotificationRepository;
import com.finance.tracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Notification Service
 * Handles sending notifications to users (email, SMS, push)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    
    /**
     * Send expense notification to user
     */
    @Override
    public void notifyExpenseCreated(User user, String description, String amount) {
        String userId = user.getUserId();
        log.info("Sending expense notification to user: {}", user.getId());
        persist(userId, "EXPENSE_CREATED", "Expense added", "Expenses recorded: "
                + description + " (" + amount +")", null);
    }
    
    /**
     * Send budget exceeded alert notification
     */
    @Override
    public void sendBudgetExceededAlert(String userId, Budget budget, BigDecimal totalSpent) {
        log.warn("Sending budget exceeded alert to user: {}", userId);

        persist(userId, "BUDGET_EXCEEDED", "Budget exceeded", "Budget '" +budget.getName() +
        "' exceeded. Limit: " + budget.getAmount() + ", spent: " + totalSpent, String.valueOf(budget.getId()));
        
    }
    
    /**
     * Send budget threshold alert notification
     */
    @Override
    public void sendBudgetThresholdAlert(String userId, Budget budget, BigDecimal totalSpent) {
        log.info("Sending budget threshold alert to user: {}", userId);
        persist(userId, "BUDGET_THRESHOLD", " Budget Threshold reached " ,
                "Budget '" +budget.getName() + "' reached threshold "
                + budget.getAlertThreshold() + "% with spent amount " + totalSpent,
                String.valueOf(budget.getId()));
    }
    
    /**
     * Send budget alert notification
     */
    @Override
    public void notifyBudgetThreshold(User user, String budgetName, Double percentageUsed) {
        String userId = user.getUserId();
        log.info("Sending budget threshold notification to user: {}", user.getId());
        persist(userId, "BUDGET_THRESHOLD", "Budget Threshold reached",
                "Budget '" + budgetName + "' is at " + percentageUsed + "% usage", null);
    }
    
    /**
     * Send income notification
     */
    @Override
    public void notifyIncomeAdded(User user, String source, String amount) {
        String userId = user.getUserId();
        log.info("Sending income notification to user: {}", user.getId());

        persist(userId, "INCOME_ADDED", "Income recorded",
                "Income added from " + source + ": " + amount, null);
    }
    
    /**
     * Send import complete notification
     */
    @Override
    public void sendImportCompleteNotification(String userId, int importedCount, int failedCount) {
        log.info("Sending import complete notification to user: {} - Imported: {}, Failed: {}", 
            userId, importedCount, failedCount);
        persist(userId, "IMPORT_COMPLETE", "Import Completed",
                "Imported " + importedCount + " item(s), failed " + failedCount + " item(s)",null);
    }

    private void persist(String userId, String type, String title, String message, String relatedEntityId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto>  getUserNotification(String userId) {
        log.debug("Fetch Notification for userId: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(notificationMapper::toDto).toList();
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(Long id) {
        log.info("Marking notification {} as read ", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: "+ id));
        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        log.info("Marking all notification as Read {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void deleteNotification(Long id) {
        log.info("Deleting Notification {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification Not Found for ID: "+ id));
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(String userId) {
        log.debug("Getting unread Notification count for user : {}", userId);
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}
