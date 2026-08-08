package com.finance.tracker.mapper;

import com.finance.tracker.dto.NotificationDto;
import com.finance.tracker.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationDto toDto(Notification notification) {
        if (notification == null)  return null;

        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public Notification toEntity(NotificationDto dto) {
        if (dto == null) return null;
        return Notification.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .relatedEntityId(dto.getRelatedEntityId())
                .isRead(dto.getIsRead())
                .build();
    }
}
