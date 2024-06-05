package org.example.creww.notification.service;


import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JwtUtils jwtUtils;
    //알림 생성
    public void createNotification(Long userId, String message) {
        Notification notification = new Notification(userId,message);
        notificationRepository.save(notification);
    }
    //알림 조회
    public List<Notification> getUserNotifications(HttpServletRequest request) {
        String token = jwtUtils.getTokenFromRequest(request);
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    //알림 읽음
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid notification ID"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }


}
