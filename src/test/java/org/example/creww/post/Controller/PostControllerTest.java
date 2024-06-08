package org.example.creww.post.Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.jwt.JwtUtils;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(PostController.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PostControllerTest {
 @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtUtils jwtUtils;
    @Autowired
    private ObjectMapper objectMapper;
    private String token;
    private Long userId;
    private Long boardId;
    public PostRequest postRequest;
    public PostResponse postResponse;

    @BeforeEach
    void setUp() {
        token = "mockToken";
        userId = 1L;
        boardId = 1L;
        postRequest = new PostRequest("Post Title", "Post Content");
        postResponse = new PostResponse(1L, "Post Title", "Post Content", userId ,"Username", LocalDateTime.now(),1);

        when(jwtUtils.getTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(userId));
    }

    @Test
    @WithMockUser
    @DisplayName("게시글 생성 테스트")
    void createPost_Test() throws Exception {
        // given
        when(postService.createPost(any(PostRequest.class), any(HttpServletRequest.class), eq(boardId)))
            .thenReturn(postResponse);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/boards/{boardId}/posts", boardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(postRequest))
            .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(postResponse.getId()))
            .andExpect(jsonPath("$.title").value(postResponse.getTitle()))
            .andExpect(jsonPath("$.content").value(postResponse.getContent()))
            .andExpect(jsonPath("$.userId").value(postResponse.getUserId()))
            .andExpect(jsonPath("$.username").value(postResponse.getUsername()));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        verify(postService, times(1)).createPost(any(PostRequest.class), any(HttpServletRequest.class), eq(boardId));
    }
    @Test
    @WithMockUser
    @DisplayName("게시글 전체 조회 테스트")
    void getPosts_Test() throws Exception {
        // given
        Long boardId = 1L;
        int page = 0;
        int size = 10;
        List<PostResponse> postResponses = Arrays.asList(
            new PostResponse(1L, "Title 1", "Content 1", 1L, "User1", LocalDateTime.now(),1),
            new PostResponse(2L, "Title 2", "Content 2", 2L,  "User2", LocalDateTime.now(),1)
        );
        Page<PostResponse> posts = new PageImpl<>(postResponses);

        when(postService.getPosts(boardId, page, size)).thenReturn(posts);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/boards/{boardId}/posts", boardId)
            .param("page", String.valueOf(page))
            .param("size", String.valueOf(size))
            .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(postResponses.get(0).getId()))
            .andExpect(jsonPath("$.content[0].title").value(postResponses.get(0).getTitle()))
            .andExpect(jsonPath("$.content[1].id").value(postResponses.get(1).getId()))
            .andExpect(jsonPath("$.content[1].title").value(postResponses.get(1).getTitle()));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        verify(postService, times(1)).getPosts(boardId, page, size);
    }
    @Test
    @WithMockUser
    @DisplayName("게시글 단일 조회 테스트")
    void getPost_Test() throws Exception {
        // given
        Long boardId = 1L;
        Long postId = 1L;
        PostResponse postResponse = new PostResponse(postId, "Title 1", "Content 1", 1L, "User1", LocalDateTime.now(),1);

        when(postService.getPost(boardId, postId)).thenReturn(postResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/boards/{boardId}/posts/{postId}", boardId, postId)
            .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(postResponse.getId()))
            .andExpect(jsonPath("$.title").value(postResponse.getTitle()));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        verify(postService, times(1)).getPost(boardId, postId);
    }
    @Test
    @WithMockUser
    @DisplayName("게시글 수정 테스트")
    void updatePost_Test() throws Exception {
        // given
        Long boardId = 1L;
        Long postId = 1L;
        PostRequest postRequest = new PostRequest("Updated Title", "Updated Content");
        String token = "mockToken"; // 가짜 토큰 생성

        // Mock HttpServletRequest
        when(jwtUtils.getTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(jwtUtils.validateToken(token)).thenReturn(true);

        // when
        mockMvc.perform(put("/api/boards/{boardId}/posts/{postId}", boardId, postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest))
                .with(csrf())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("게시글 수정이 완료 되었습니다."));

        // then
        verify(postService, times(1)).updatePost(eq(boardId), eq(postId), any(HttpServletRequest.class), any(PostRequest.class));
    }
    @Test
    @WithMockUser
    @DisplayName("게시글 삭제 테스트")
    void deletePost_Test() throws Exception {
        // given
        Long postId = 1L;
        String token = "mockToken"; // 가짜 토큰 생성

        // Mock HttpServletRequest
        when(jwtUtils.getTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(jwtUtils.validateToken(token)).thenReturn(true);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/boards/posts/{postId}", postId) // delete 요청 보냄
            .with(csrf())); // csrf 설정 통과

        // then
        resultActions.andExpect(status().isOk()); // 상태 코드 검증
        verify(postService, times(1)).deletePost(eq(postId), any(HttpServletRequest.class)); // deletePost 메소드가 1번 호출되었는지 검증
    }
}
