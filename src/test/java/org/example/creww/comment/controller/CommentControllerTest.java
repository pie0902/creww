package org.example.creww.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.comment.dto.CommentRequest;
import org.example.creww.comment.dto.CommentResponse;
import org.example.creww.comment.service.CommentService;
import org.example.creww.jwt.JwtUtils;
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

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtUtils jwtUtils;
    @Autowired
    private ObjectMapper objectMapper;
    private String token;
    private Long userId;
    private Long postId;
    private Long boardId;
    public CommentRequest commentRequest;
    public CommentResponse commentResponse;
    public String username;
    @BeforeEach
    void setUp() {
        token = "mockToken";
        userId = 1L;
        postId = 1L;
        boardId = 1L;
        username = "testUser";
        commentRequest = new CommentRequest("test comment");
        commentResponse = new CommentResponse(1L, "test comment",username);

        when(jwtUtils.getTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(userId));
    }
    @Test
    @WithMockUser
    @DisplayName("댓글 생성 테스트")
    void createComment_Test() throws Exception {
        // given
        when(commentService.createComment(any(HttpServletRequest.class), eq(postId),
            any(CommentRequest.class)))
            .thenReturn(commentResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
            post("/api/boards/{boardId}/posts/{postId}/comments", boardId, postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(commentRequest))
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentResponse.getId()))
            .andExpect(jsonPath("$.content").value(commentResponse.getContent()))
            .andExpect(jsonPath("$.username").value(commentResponse.getUsername()));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        verify(commentService, times(1)).createComment(any(HttpServletRequest.class), eq(postId),
            any(CommentRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 조회 테스트")
    void getComments_Test() throws Exception {
        // given
        Long boardId = 1L;
        Long postId = 1L;
        String username = "tester1";

        List<CommentResponse> commentResponses = Arrays.asList(
            new CommentResponse(1L, "test1", username),
            new CommentResponse(2L, "test2", "tester2")
        );

        when(commentService.getComments(any(HttpServletRequest.class), eq(postId))).thenReturn(commentResponses);

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/boards/{boardId}/posts/{postId}/comments", boardId, postId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$[0].content").value("test1"))
            .andExpect(jsonPath("$[1].content").value("test2"));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        verify(commentService, times(1)).getComments(any(HttpServletRequest.class), eq(postId));
    }


}
