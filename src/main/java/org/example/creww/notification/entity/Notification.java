package org.example.creww.notification.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor
@Getter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String message;
    private boolean isRead = false;
    private LocalDateTime createdAt;


    public Notification(Long userId,String message) {
        this.userId = userId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
    public void setIsRead(boolean yn){
        this.isRead = yn;
    }
}
