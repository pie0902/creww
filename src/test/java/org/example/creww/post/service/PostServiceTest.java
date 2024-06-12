package org.example.creww.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.board.entity.Board;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.notification.service.NotificationDomainService;
import org.example.creww.post.dto.PostRequest;
import org.example.creww.post.dto.PostResponse;
import org.example.creww.post.entity.Post;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.example.creww.userBoard.repository.UserBoardRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBoardRepository userBoardRepository;
    @Mock
    private NotificationDomainService notificationDomainService;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private PostService postService;


    private String token;
    private User user;
    private Board board;
    private Post post;
    private PostRequest postRequest;
    private PostResponse postResponse;
    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "tester", "1234");
        ReflectionTestUtils.setField(user, "id", 1L);
        board = new Board("Test Board", "Test Description", user.getId());
        ReflectionTestUtils.setField(board, "id", 1L);
        token = "testToken";
        post = new Post("title", "content", user.getId(), board.getId());
        ReflectionTestUtils.setField(post, "id", 1L);
        postRequest = new PostRequest("title","content");
        postResponse = new PostResponse(1L,"title","content",user.getId(),user.getUsername(),
            LocalDateTime.now(),0);
    }

    @Test
    @DisplayName("포스트 작성 테스트")
    void createPost_test() {
        //given
        Long boardId = board.getId();
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(jwtUtils.getTokenFromRequest(mockRequest)).thenReturn(token);
        when(jwtUtils.isTokenValid(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedPost, "id", 1L); //저장된 포스트 객체의 id를 1L로 설정
            return savedPost; //반환
        });
        // When
        PostResponse response = postService.createPost(postRequest, mockRequest, boardId);

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
    @DisplayName("포스트 전체 조회 테스트")
    void getPosts_test() {
        // given
        int page = 0; // 페이지 번호는 0부터 시작
        int size = 1;

        List<Post> posts = Collections.singletonList(new Post("title", "content", user.getId(),board.getId()));
        Page<Post> postPage = new PageImpl<>(posts, PageRequest.of(page, size), posts.size());

        when(postRepository.findByBoardId(board.getId(), PageRequest.of(page, size)))
            .thenReturn(postPage);

        when(userRepository.findById(posts.get(0).getUserId()))
            .thenReturn(Optional.of(new User(user.getEmail(), user.getUsername(),user.getPassword())));

        // when
        Page<PostResponse> postResponses = postService.getPosts(board.getId(), page, size);

        // then
        assertEquals(1, postResponses.getTotalElements());
        PostResponse postResponse = postResponses.getContent().get(0);
        assertEquals("title", postResponse.getTitle());
        assertEquals("content", postResponse.getContent());
        assertEquals("tester", postResponse.getUsername());

        verify(postRepository, times(1)).findByBoardId(board.getId(), PageRequest.of(page, size));
        verify(userRepository, times(1)).findById(posts.get(0).getUserId());
    }

    @Test
    @DisplayName("포스트 단일 조회 테스트")
    void getPost_test(){
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
    @DisplayName("포스트 삭제 테스트")
    void deletePost_test() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.getTokenFromRequest(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        // when
        postService.deletePost(post.getId(), request);

        // then
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("포스트 수정 테스트")
    void updatePost_test() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Long userId = user.getId();
        Long postId = post.getId();
        PostRequest postRequest1 = new PostRequest("title2", "content2");

        when(jwtUtils.getTokenFromRequest(request)).thenReturn(token);
        when(jwtUtils.isTokenValid(token)).thenReturn(true);
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
