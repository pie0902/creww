package org.example.creww.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.post.entity.Post;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.example.creww.userBoard.entity.UserBoard;
import org.example.creww.userBoard.repository.UserBoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

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
    NotificationService notificationService;

    @Test
    @DisplayName("알림 가져오기 테스트")
    void getNotification_test(){
        Long boardId = 1L;
        Long userId =1L;
        Post post = new Post("test","test",userId,boardId);
        ReflectionTestUtils.setField(post,"id",1L);
        User user = new User("test@test.com","tester","1234");
        ReflectionTestUtils.setField(user,"id",1L);
        UserBoard userBoard = new UserBoard(1L,1L);
        List<UserBoard> userBoards = Arrays.asList(userBoard);
        when(userBoardRepository.findByBoardIdAndIsExitedFalse(boardId)).thenReturn(userBoards);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        notificationDomainService.giveNotification(boardId,post.getId());


        // Then
        verify(notificationService, times(1)).createNotification(
            eq(userBoard.getUserId()),
            eq(user.getUsername() + "님이 " + post.getTitle() + " 게시글을 작성 하셨습니다.")
        );
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
