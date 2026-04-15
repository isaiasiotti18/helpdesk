package com.helpdesk.backend.modules.notification.application.dtos;

import com.helpdesk.backend.modules.notification.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String type,
        String title,
        String content,
        String ticketId,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId().toString(),
                n.getType(),
                n.getTitle(),
                n.getContent(),
                n.getTicketId() != null ? n.getTicketId().toString() : null,
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}
