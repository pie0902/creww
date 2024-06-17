package org.example.creww.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.comment.dto.CommentRequest;
import org.example.creww.comment.dto.CommentResponse;
import org.example.creww.comment.entity.Comment;
import org.example.creww.comment.repository.CommentRepository;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @InjectMocks
    CommentService commentService;
    @Mock
    CommentRepository commentRepository;
    @Mock
    JwtUtils jwtUtils;
    @Mock
    UserRepository userRepository;

    private Long postId;
    private Long boardId;
    private User user;
    private HttpServletRequest httpServletRequest;
    private Comment comment;
    private String token;
    @BeforeEach
    void setUp() {
        postId = 1L;
        boardId = 1L;
        token = "testToken";
        user = new User("test@test.com", "tester", "1234");
        ReflectionTestUtils.setField(user, "id", 1L);
        httpServletRequest = mock(HttpServletRequest.class);
        comment = new Comment("test", user.getUsername(), postId, user.getId());
        ReflectionTestUtils.setField(comment, "id", 1L);
    }

    @Test
    @DisplayName("댓글 생성 테스트")
    void createComment_test() {
        //given
        when(jwtUtils.validateTokenOrThrow(httpServletRequest)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        CommentRequest commentRequest = new CommentRequest("test");
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saveComment = invocation.getArgument(0);
            ReflectionTestUtils.setField(saveComment, "id", 1L); //저장된 포스트 객체의 id를 1L로 설정
            return saveComment;// ; //반환
        });
        //when
        CommentResponse commentResponse = commentService.createComment(httpServletRequest, postId,
            commentRequest);

        //then
        verify(commentRepository, times(1)).save(any(Comment.class));
        assertEquals(1L, commentResponse.getId());
        assertEquals(commentResponse.getContent(), comment.getContent());
        assertEquals(commentResponse.getUsername(), comment.getUsername());
    }
    @Test
    @DisplayName("댓글 가져오기")
    void getComments_test() {
        //given
        List<Comment> comments = Arrays.asList(comment);
        when(commentRepository.findByPostId(postId)).thenReturn(comments);
        when(jwtUtils.validateTokenOrThrow(httpServletRequest)).thenReturn(token);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        //when
        List<CommentResponse> commentResponses = commentService.getComments(httpServletRequest,
            postId);

        //then
        assertEquals(commentResponses.get(0).getUsername(), comments.get(0).getUsername());
        assertEquals(commentResponses.get(0).getId(), comments.get(0).getId());
        assertEquals(commentResponses.get(0).getContent(), comments.get(0).getContent());
    }
    @Test
    @DisplayName("댓글 지우기 테스트")
    void deleteComment_test() {
        //given
        when(jwtUtils.validateTokenOrThrow(httpServletRequest)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));


        //when
        commentService.deleteComment(httpServletRequest, comment.getId(), postId);

        //then
        verify(commentRepository, times(1)).delete(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void updateComment() {
        //given
        CommentRequest commentRequest = new CommentRequest("update test");
        when(jwtUtils.validateTokenOrThrow(httpServletRequest)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        //when
        commentService.updateComment(httpServletRequest, postId, comment.getId(), commentRequest);

        //then
        verify(commentRepository, times(1)).save(any(Comment.class));
        assertEquals(comment.getContent(), commentRequest.getContent());
    }



}
