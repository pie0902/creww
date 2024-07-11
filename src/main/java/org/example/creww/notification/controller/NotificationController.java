package org.example.creww.notification.controller;


import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.service.NotificationDomainService;
import org.example.creww.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationDomainService notificationDomainService;

    @GetMapping
    @ApiOperation(value = "알림 가져오기", notes = "알림을 가져옵니다.", tags = {"notification-controller"})
    public ResponseEntity<List<Notification>> getUserNotifications(HttpServletRequest request) {
        List<Notification> notifications = notificationService.getUserNotifications(request);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    @ApiOperation(value = "알림 읽기", notes = "알림을 읽음 표시 합니다.", tags = {"notification-controller"})
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/readAll")
    @ApiOperation(value = "알림 모두읽기", notes = "알림을 모두 읽음 표시 합니다.",tags = {"notification-controller"})
    public ResponseEntity<Void> markAsReadAll(HttpServletRequest request){
        notificationService.markAsReadAll(request);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/subscribe")
    public SseEmitter subscribe(Long userId) {
        return notificationDomainService.subscribe(userId);
    }
}
