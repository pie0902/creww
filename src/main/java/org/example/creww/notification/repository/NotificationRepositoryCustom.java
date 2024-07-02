package org.example.creww.notification.repository;

import java.util.List;
import org.example.creww.notification.entity.Notification;

public interface NotificationRepositoryCustom {
    void bulkInsert(List<Notification> notifications);
}
