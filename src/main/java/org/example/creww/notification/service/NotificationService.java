package org.example.creww.notification.service;


import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JwtUtils jwtUtils;
    //알림 조회
    public List<Notification> getUserNotifications(HttpServletRequest request) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    //알림 읽음
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ApplicationException("Invalid notification ID", HttpStatus.NOT_FOUND));
        notification.setIsRead();
    }

    public void markAsReadAll(HttpServletRequest request) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
