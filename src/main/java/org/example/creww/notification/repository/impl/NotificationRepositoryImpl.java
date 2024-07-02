package org.example.creww.notification.repository.impl;


import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.repository.NotificationRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import javax.persistence.EntityManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class NotificationRepositoryImpl implements NotificationRepositoryCustom{

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    @Autowired
    public NotificationRepositoryImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @Override
    public void bulkInsert(List<Notification> notifications) {
        String sql = "INSERT INTO notification (user_id, message, created_at, is_read) VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Notification notification = notifications.get(i);
                ps.setLong(1, notification.getUserId());
                ps.setString(2, notification.getMessage());
                ps.setTimestamp(3, Timestamp.valueOf(notification.getCreatedAt()));
                ps.setBoolean(4, notification.isRead());
            }

            @Override
            public int getBatchSize() {
                return notifications.size();
            }
        });
    }
}
