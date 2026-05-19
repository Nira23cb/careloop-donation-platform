package com.careloop.controller;

import com.careloop.model.Notification;
import com.careloop.security.UserContext;
import com.careloop.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification bell API.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> list() {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new RuntimeException("Please login first");
        return ResponseEntity.ok(notificationService.getForUser(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new RuntimeException("Please login first");
        Map<String, Long> body = new HashMap<>();
        body.put("count", notificationService.getUnreadCount(userId));
        return ResponseEntity.ok(body);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new RuntimeException("Please login first");
        notificationService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }
}
