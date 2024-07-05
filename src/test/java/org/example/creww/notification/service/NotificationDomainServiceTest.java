package org.example.creww.notification.service;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.repository.NotificationRepository;
import org.example.creww.post.dto.PostWithUser;
import org.example.creww.post.entity.Post;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.example.creww.userBoard.entity.UserBoard;
import org.example.creww.userBoard.repository.UserBoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDomainServiceTest {
    @InjectMocks
    NotificationDomainService notificationDomainService;
    @Mock
    UserBoardRepository userBoardRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    NotificationService notificationService;
    @Mock
    UserRepository userRepository;
    @Mock
    NotificationRepository notificationRepository;
    private HttpServletRequest httpServletRequest;
    private Long boardId;
    private String username;
    private User user;
    private Post post;
    private PostWithUser postWithUser;
    private Notification notification;
    private List<Long> userIds;

    @BeforeEach
    void setUp() {
        httpServletRequest = mock(HttpServletRequest.class);
        boardId = 1L;
        username = "tester";
        user = new User("test@test.com", "tester", "1234");
        ReflectionTestUtils.setField(user, "id", 1L);
        post = new Post("test", "test", user.getId(), boardId);
        ReflectionTestUtils.setField(post, "id", 1L);
        postWithUser = new PostWithUser(post.getId(), post.getTitle(), user.getId(), username);
        notification = new Notification(1L, "게시글이 생성 되었습니다");
        userIds = Arrays.asList(1L);
    }

    @Test
    @DisplayName("알림 가져오기 테스트")
    void getNotification_test() {
        // Given
        assertNotNull(postRepository);

        when(userBoardRepository.findUserIdsByBoardIdAndIsExitedFalse(boardId)).thenReturn(userIds);
        when(postRepository.findPostWithUserById(anyLong())).thenReturn(Optional.of(postWithUser));
        doNothing().when(notificationRepository).bulkInsert(any());

        // When & Then
        assertDoesNotThrow(() -> notificationDomainService.giveNotification(boardId, post.getId()));

        // Verify
        verify(postRepository).findPostWithUserById(anyLong());
        verify(userBoardRepository).findUserIdsByBoardIdAndIsExitedFalse(boardId);
        verify(notificationRepository).bulkInsert(any());
    }

    @Test
    @DisplayName("게시글이 없을 때 예외 발생 테스트")
    void getNotification_postNotFound_test() {
        // Given
        when(postRepository.findPostWithUserById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            notificationDomainService.giveNotification(boardId, post.getId())
        );
        assertEquals("게시글 없음", exception.getMessage());
    }

    @Test
    @DisplayName("게시글이 없을 때 예외 발생 테스트")
    void giveNotification_postNotFound_test() {
        // Given
        Long postId = 1L;
        when(postRepository.findPostWithUserById(postId)).thenReturn(Optional.empty());

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            notificationDomainService.giveNotification(1L, postId)
        );
        assertEquals("게시글 없음", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
    @Test
    @DisplayName("notificationRepository가 null일 때 예외 발생 테스트")
    void giveNotification_notificationRepositoryNull_test() {
        // Given
        Long boardId = 1L;
        Long postId = 1L;
        PostWithUser postWithUser = new PostWithUser(postId, "test title", user.getId(), user.getUsername());
        List<Long> userIds = Arrays.asList(1L, 2L);

        when(postRepository.findPostWithUserById(postId)).thenReturn(Optional.of(postWithUser));
        when(userBoardRepository.findUserIdsByBoardIdAndIsExitedFalse(boardId)).thenReturn(userIds);

        // notificationRepository를 null로 설정
        ReflectionTestUtils.setField(notificationDomainService, "notificationRepository", null);

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class, () ->
            notificationDomainService.giveNotification(boardId, postId)
        );
        assertEquals("notificationRepository is null", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());

        // 다시 원래의 notificationRepository로 복원
        ReflectionTestUtils.setField(notificationDomainService, "notificationRepository", notificationRepository);

        // Verify
        verify(postRepository).findPostWithUserById(postId);
        verify(userBoardRepository).findUserIdsByBoardIdAndIsExitedFalse(boardId);
    }

}
