package org.example.creww.notification.repository;

import java.util.List;
import org.example.creww.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository <Notification,Long>{
//    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
}
