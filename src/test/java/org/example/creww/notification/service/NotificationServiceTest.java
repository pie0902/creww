package org.example.creww.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.repository.NotificationRepository;
import org.example.creww.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {


    @InjectMocks
    NotificationService notificationService;
    @Mock
    NotificationRepository notificationRepository;
    @Mock
    JwtUtils jwtUtils;

    private HttpServletRequest request;
    private String token;
    private Long userId;
    private Notification notification;
    private User user;
    @BeforeEach
    void setup() {
        request = mock(HttpServletRequest.class);
        token = "testToken";
        userId = 1L;
        user = new User("test@test.com", "tester", "encodedPassword");
        ReflectionTestUtils.setField(user, "id", userId);

        notification = new Notification(userId,"test");
        ReflectionTestUtils.setField(notification, "id", 1L);
        ReflectionTestUtils.setField(notification, "isRead", false);
    }


    @Test
    @DisplayName("알림 생성 테스트")
    void createNotification_test() {
        //when&then
        notificationService.createNotification(userId, "test");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
    @Test
    @DisplayName("알림 가져오기 테스트")
    void getUserNotification_test() {
        //given
        List<Notification> notifications = Arrays.asList(notification);
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(userId));
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)).thenReturn(
            notifications);

        //when
        List<Notification> notificationList = notificationService.getUserNotifications(
            request);

        //then
        verify(notificationRepository, times(1)).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        assertEquals(notification.getMessage(), notificationList.get(0).getMessage());
    }
    @Test
    @DisplayName("알림 읽음 처리 테스트 - 성공")
    void markAsRead_success_test() {
        // given
        when(notificationRepository.findById(notification.getId())).thenReturn(
            Optional.of(notification));

        // when
        notificationService.markAsRead(notification.getId());

        // then
        assertEquals(true, notification.isRead());
    }
    @Test
    @DisplayName("알림 읽음 처리 테스트 - 실패")
    void markAsRead_failure_test() {
        // given
        Long invalidId = 99L;
        when(notificationRepository.findById(invalidId)).thenReturn(Optional.empty());

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            notificationService.markAsRead(invalidId);
        });

        assertEquals("Invalid notification ID", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

}
