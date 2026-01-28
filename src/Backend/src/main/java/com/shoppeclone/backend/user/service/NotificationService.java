package com.shoppeclone.backend.user.service;

import com.shoppeclone.backend.user.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    void createNotification(String userId, String title, String message, String type);

    List<NotificationResponse> getUserNotifications(String email);

    void markAsRead(String notificationId);

    void markAllAsRead(String email);
}
