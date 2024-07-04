package org.example.creww.notification.service;

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
    UserRepository userRepository;
    @Mock
    NotificationRepository notificationRepository;

    private Long boardId;
    private String username;
    private User user;
    private Post post;
    private PostWithUser postWithUser;
    private Notification notification;
    private List<Long> userIds;

    @BeforeEach
    void setUp() {
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
    @DisplayName("존재하지 않는 포스트")
    void getNotification_null_post_test(){
        Long boardId = 1L;
        Long userId =1L;
        Post post = new Post("test","test",userId,boardId);
        ReflectionTestUtils.setField(post,"id",1L);
        User user = new User("test@test.com","tester","1234");
        ReflectionTestUtils.setField(user,"id",1L);
        UserBoard userBoard = new UserBoard(1L,1L);
        List<UserBoard> userBoards = Arrays.asList(userBoard);
        when(userBoardRepository.findByBoardIdAndIsExitedFalse(boardId)).thenReturn(userBoards);
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        ApplicationException applicationException = assertThrows(ApplicationException.class,()-> {
            notificationDomainService.giveNotification(boardId,post.getId());
        });

        assertEquals("존재하지 않는 post",applicationException.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,applicationException.getStatus());

    }
    @Test
    @DisplayName("존재하지 않는 유저")
    void getNotification_null_user_test(){
        Long boardId = 1L;
        Long userId =1L;
        Post post = new Post("test","test",userId,boardId);
        ReflectionTestUtils.setField(post,"id",1L);
        User user = new User("test@test.com","tester","1234");
        ReflectionTestUtils.setField(user,"id",1L);
        UserBoard userBoard = new UserBoard(1L,1L);
        List<UserBoard> userBoards = Arrays.asList(userBoard);
        when(userBoardRepository.findByBoardIdAndIsExitedFalse(boardId)).thenReturn(userBoards);
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ApplicationException applicationException = assertThrows(ApplicationException.class,()-> {
            notificationDomainService.giveNotification(boardId,post.getId());
        });

        assertEquals("존재하지 않는 user",applicationException.getMessage());
        assertEquals(HttpStatus.NOT_FOUND,applicationException.getStatus());
    }

}
