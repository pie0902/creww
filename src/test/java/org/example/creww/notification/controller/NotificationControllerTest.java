package org.example.creww.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.service.NotificationDomainService;
import org.example.creww.notification.service.NotificationService;
import org.example.creww.post.dto.PostRequest;
import org.example.creww.post.dto.PostResponse;
import org.example.creww.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(NotificationController.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationDomainService notificationDomainService;

    @MockBean
    private JwtUtils jwtUtils;

    private String token;
    private Long userId;

    @BeforeEach
    void setUp() {
        token = "mockToken";
        userId = 1L;

        when(jwtUtils.getTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(userId));

    }

    @Test
    @WithMockUser
    @DisplayName("알림 가져오기 테스트")
    void notification_test() throws Exception {
        // given
        List<Notification> notifications = Arrays.asList(
            new Notification(userId, "test message 1"),
            new Notification(userId, "test message 2")
        );
        when(notificationService.getUserNotifications(any(HttpServletRequest.class))).thenReturn(
            notifications);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/notifications")
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf()));
        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[0].message").value("test message 1"))
            .andExpect(jsonPath("$[1].userId").value(userId))
            .andExpect(jsonPath("$[1].message").value("test message 2"));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        verify(notificationService, times(1)).getUserNotifications(any(HttpServletRequest.class));
    }
    @Test
    @WithMockUser
    @DisplayName("알림 읽음 표시 테스트")
    public void testMarkAsRead() throws Exception {
        Long notificationId = 1L;

        ResultActions resultActions = mockMvc.perform(put("/api/notifications/{notificationId}/read", notificationId)
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf())); // csrf 설정 통과

        resultActions.andExpect(status().isNoContent());

        // Verify that the service method was called
        verify(notificationService).markAsRead(notificationId);
    }
    @Test
    @WithMockUser
    @DisplayName("알림 모두 읽기 테스트")
    void markAsReadAll_test() throws Exception {
        // given
        doNothing().when(notificationService).markAsReadAll(any(HttpServletRequest.class));

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/notifications/readAll")
            .with(csrf()));

        // then
        resultActions
            .andExpect(status().isNoContent());

        verify(notificationService, times(1)).markAsReadAll(any(HttpServletRequest.class));
    }
}

