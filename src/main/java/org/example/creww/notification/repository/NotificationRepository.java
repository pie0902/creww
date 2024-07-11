package org.example.creww.notification.repository;

import java.util.List;
import java.util.Map;
import org.example.creww.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository <Notification,Long> ,
    NotificationRepositoryCustom {
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    @Query(value = "SELECT user_id as userId, COUNT(*) as count FROM notifications WHERE user_id IN :userIds AND is_read = false GROUP BY user_id", nativeQuery = true)
    Map<Long, Long> countNewNotificationsByUserIds(@Param("userIds") List<Long> userIds);
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(Long userId);


}
