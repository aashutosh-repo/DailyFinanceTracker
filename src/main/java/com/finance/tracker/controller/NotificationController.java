package com.finance.tracker.controller;

import com.finance.tracker.dto.NotificationDto;
import com.finance.tracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getUserNotification(@RequestParam String userId) {
        List<NotificationDto> notificationDtos = notificationService.getUserNotification(userId);
        return ResponseEntity.ok(notificationDtos);
    }

    @GetMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {
        NotificationDto notificationDto = notificationService.markAsRead(id);
        return ResponseEntity.ok(notificationDto);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam String  userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread")
    public ResponseEntity<Long> getUnreadCount(@RequestParam String userId) {
        Long unreadCount = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(unreadCount);
    }
}
