package org.example.creww.notification.controller;


import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController{
    private final NotificationService notificationService;
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(HttpServletRequest request) {
        List<Notification> notifications = notificationService.getUserNotifications(request);
        return ResponseEntity.ok(notifications);
    }
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }
}
