package com.finance.tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDto {
    private long id;
    private String userId;
    private String type;
    private String title;
    private String message;
    private String relatedEntityId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
