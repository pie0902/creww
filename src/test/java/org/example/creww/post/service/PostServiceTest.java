package org.example.creww.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.board.entity.Board;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.notification.service.NotificationDomainService;
import org.example.creww.post.dto.PostRequest;
import org.example.creww.post.dto.PostResponse;
import org.example.creww.post.entity.Post;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationDomainService notificationDomainService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private PostService postService;

    private String token;
    private User user;
    private User user2;
    private Board board;
    private Post post;
    private PostRequest postRequest;
    private PostResponse postResponse;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "tester", "1234");
        ReflectionTestUtils.setField(user, "id", 1L);
        user2 = new User("test2@test.com", "tester2", "1234");
        ReflectionTestUtils.setField(user2, "id", 2L);

        board = new Board("Test Board", "Test Description", user.getId());
        ReflectionTestUtils.setField(board, "id", 1L);

        post = new Post("title", "content", user.getId(), board.getId());
        ReflectionTestUtils.setField(post, "id", 1L);

        postRequest = new PostRequest("title", "content");
        postResponse = new PostResponse(1L, "title", "content", user.getId(), user.getUsername(),
            LocalDateTime.now(), 0);

        request = mock(HttpServletRequest.class);
    }

    @Test
    @DisplayName("포스트 작성 테스트")
    void createPost_test() {
        //given
        Long boardId = board.getId();
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(userRepository.findUsernameById(user.getId())).thenReturn(Optional.of(user.getUsername()));

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedPost, "id", 1L); //저장된 포스트 객체의 id를 1L로 설정
            return savedPost; //반환
        });
        // When
        PostResponse response = postService.createPost(postRequest, request, boardId);

        // Then
        verify(postRepository).save(any(Post.class));
        verify(notificationDomainService).giveNotification(boardId, post.getId());

        assertNotNull(response);
        assertEquals(postResponse.getId(), response.getId());
        assertEquals(postResponse.getTitle(), response.getTitle());
        assertEquals(postResponse.getContent(), response.getContent());
        assertEquals(postResponse.getUserId(), response.getUserId());
        assertEquals(postResponse.getUsername(), response.getUsername());
    }

    @Test
    @DisplayName("포스트 작성 유저 없음 오류 테스트")
    void createPost_error_test() {
        //given
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(userRepository.findUsernameById(user.getId())).thenReturn(Optional.of(user.getUsername()));

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedPost, "id", null); //저장된 포스트 객체의 id를 null로 설정
            return savedPost; //반환
        });

        //when
        ApplicationException createPostException = assertThrows(ApplicationException.class, () ->
            postService.createPost(postRequest,request, board.getId()));

        //then
        assertEquals("Failed to save the post, ID is null!", createPostException.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, createPostException.getStatus());
    }

    @Test
    @DisplayName("토큰 오류 테스트")
    void post_token_error_test() {
        // Token 설정
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.validateTokenOrThrow(request)).thenThrow(new ApplicationException("Invalid token", HttpStatus.UNAUTHORIZED));

        // createPost isTokenValid 오류
        ApplicationException createPostException = assertThrows(ApplicationException.class, () ->
            postService.createPost(postRequest, request, board.getId()));

        // updatePost isTokenValid 오류
        ApplicationException updatePostException = assertThrows(ApplicationException.class, () ->
            postService.updatePost(board.getId(), post.getId(), request, postRequest));

        assertEquals("Invalid token", createPostException.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, createPostException.getStatus());
        assertEquals("Invalid token", updatePostException.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, updatePostException.getStatus());
    }





    @Test
    @DisplayName("포스트 전체 조회 테스트")
    void getPosts_test() {
        // given
        int page = 0;
        int size = 1;
        Pageable pageable = PageRequest.of(page, size);
        List<Post> postList = Arrays.asList(post);
        Page<Post> posts = new PageImpl<>(postList, pageable, postList.size());

        // 유저 관련
        List<User> users = Arrays.asList(user);

        // 레포지토리
        when(postRepository.findByBoardId(eq(board.getId()), eq(pageable))).thenReturn(posts);
        when(userRepository.findAllById(any())).thenReturn(users);

        // when
        Page<PostResponse> result = postService.getPosts(board.getId(), page, size);

        // then
        verify(postRepository).findByBoardId(eq(board.getId()), eq(pageable));
        verify(userRepository).findAllById(any());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        PostResponse postResponse = result.getContent().get(0);
        assertEquals("title", postResponse.getTitle());
        assertEquals("content", postResponse.getContent());
        assertEquals(post.getUserId(), postResponse.getUserId());
        assertEquals(user.getUsername(), postResponse.getUsername());
    }

    @Test
    @DisplayName("포스트 단일 조회 테스트")
    void getPost_test() {
        //given
        when(userRepository.findById(post.getUserId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        //when
        PostResponse postResponse1 = postService.getPost(board.getId(), post.getId());

        //then
        assertEquals("title", postResponse1.getTitle());
        assertEquals("content", postResponse1.getContent());
        assertEquals(user.getUsername(), postResponse1.getUsername());
        assertEquals(post.getViews(), postResponse1.getViews());

        verify(postRepository, times(1)).findById(post.getId());
        verify(userRepository, times(1)).findById(post.getUserId());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("포스트 단일 조회 오류 테스트")
    void getPost_error_test() {
        //given
        Long testBoardId = 2L;
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        //when&then
        ApplicationException getPostException = assertThrows(ApplicationException.class, () ->
            postService.getPost(testBoardId, post.getId()));

        assertEquals("올바른 요청이 아닙니다", getPostException.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, getPostException.getStatus());
    }

    @Test
    @DisplayName("포스트 삭제 테스트")
    void deletePost_test() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        // when
        postService.deletePost(post.getId(), request);

        // then
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("포스트 삭제 오류 테스트")
    void deletePostUpdatePost_error_test() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(2L)); // 권한 없는 유저
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        Long testBoardId = 2L;

        //when&then deletePost
        ApplicationException deletePostException = assertThrows(ApplicationException.class, () ->
            postService.deletePost(post.getId(), request));
        assertEquals("자신이 작성한 게시물글만 삭제 가능 합니다", deletePostException.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, deletePostException.getStatus());

        //when&then updatePost
        ApplicationException updatePostException = assertThrows(ApplicationException.class, () ->
            postService.updatePost(testBoardId, post.getId(), request, postRequest));
        assertEquals("잘못된 게시글 입니다.", updatePostException.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, updatePostException.getStatus());
    }

    @Test
    @DisplayName("updatePost 권한 오류 테스트")
    void updatePost_error_test() {
        //given
        PostRequest postRequest1 = new PostRequest("title2", "content2");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(2L)); // 권한 없는 유저
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        // when & then
        ApplicationException updatePostException = assertThrows(ApplicationException.class, () ->
            postService.updatePost(board.getId(), post.getId(), request, postRequest1));
        assertEquals("권한이 없습니다.", updatePostException.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, updatePostException.getStatus());
    }

    @Test
    @DisplayName("포스트 수정 테스트")
    void updatePost_test() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Long userId = user.getId();
        Long postId = post.getId();
        PostRequest postRequest1 = new PostRequest("title2", "content2");

        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(userId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        postService.updatePost(board.getId(), post.getId(), request, postRequest1);

        // then
        verify(postRepository, times(1)).save(post);
        assertEquals(post.getTitle(), postRequest1.getTitle());
        assertEquals(post.getContent(), postRequest1.getContent());
    }
}
