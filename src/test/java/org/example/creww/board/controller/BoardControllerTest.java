package org.example.creww.board.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.board.dto.BoardAddUserRequest;
import org.example.creww.board.dto.BoardRequest;
import org.example.creww.board.dto.BoardResponse;
import org.example.creww.board.service.BoardService;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(BoardController.class)
@ActiveProfiles("test")
class BoardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BoardService boardService;

    @MockBean
    JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long userId;
    private BoardRequest boardRequest;

    @BeforeEach
    void setUp() {
        token = "mockToken";
        userId = 1L;
        boardRequest = new BoardRequest("테스트", Arrays.asList(2L, 3L), "test board");

        Mockito.when(jwtUtils.getTokenFromRequest(Mockito.any(HttpServletRequest.class)))
            .thenReturn(token);
        Mockito.when(jwtUtils.validateToken(token)).thenReturn(true);
        Mockito.when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(userId));
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 생성 테스트")
    void createBoard_Test() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/api/boards/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardRequest))
            .with(csrf()));

        resultActions.andExpect(status().isOk())
            .andExpect(content().string("성공적으로 보드가 생성 되었습니다"));
        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);
        verify(boardService, times(1)).createBoard(Mockito.any(HttpServletRequest.class), Mockito.any(BoardRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 전체 조회 테스트")
    void getBoards_Test() throws Exception {
        List<BoardResponse> boardResponses = Arrays.asList(
            new BoardResponse("Board 1", 1L, "Description 1", "tester1"),
            new BoardResponse("Board 2", 2L, "Description 2", "tester2")
        );
        Mockito.when(boardService.getBoards(Mockito.any(HttpServletRequest.class)))
            .thenReturn(boardResponses);

        ResultActions resultActions = mockMvc.perform(get("/api/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf()));

        for (int i = 0; i < boardResponses.size(); i++) {
            BoardResponse board = boardResponses.get(i);
            resultActions.andExpect(
                    jsonPath(String.format("$[%d].id", i), is(board.getId().intValue())))
                .andExpect(jsonPath(String.format("$[%d].boardName", i), is(board.getBoardName())))
                .andExpect(
                    jsonPath(String.format("$[%d].description", i), is(board.getDescription())));
        }

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        verify(boardService, times(1)).getBoards(Mockito.any(HttpServletRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 단일 조회 테스트")
    void getBoard_Test() throws Exception {
        BoardResponse boardResponse = new BoardResponse("Board 1", 1L, "Description 1", "tester1");
        Mockito.when(boardService.getBoard(1L)).thenReturn(boardResponse);

        ResultActions resultActions = mockMvc.perform(get("/api/boards/1")
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf()));

        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.boardName").value("Board 1"))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.description").value("Description 1"))
            .andExpect(jsonPath("$.ownerName").value("tester1"));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);

        verify(boardService, times(1)).getBoard(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 유저 초대 테스트")
    void addUser_Test() throws Exception {
        List<Long> userIds = Arrays.asList(1L, 2L);
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(userIds);

        doNothing().when(boardService).addUser(Mockito.any(HttpServletRequest.class), Mockito.any(BoardAddUserRequest.class), Mockito.eq(1L));

        ResultActions resultActions = mockMvc.perform(post("/api/boards/1/addUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardAddUserRequest))
            .with(csrf()));

        resultActions.andExpect(status().isOk())
            .andExpect(content().string("성공적으로 초대가 되었습니다"));
        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);
        verify(boardService, times(1)).addUser(Mockito.any(HttpServletRequest.class), Mockito.any(BoardAddUserRequest.class), Mockito.eq(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 삭제 테스트")
    void deleteBoard_Test() throws Exception {
        Long boardId = 1L;
        doNothing().when(boardService).deleteBoard(Mockito.any(HttpServletRequest.class), Mockito.eq(boardId));

        ResultActions resultActions = mockMvc.perform(delete("/api/boards/{boardId}", boardId)
            .with(csrf()));

        resultActions.andExpect(status().isOk());
        verify(boardService, times(1)).deleteBoard(Mockito.any(HttpServletRequest.class), Mockito.eq(boardId));
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 탈퇴 테스트")
    void exitBoard_Test() throws Exception {
        Long boardId = 1L;
        doNothing().when(boardService).isExitBoard(Mockito.any(HttpServletRequest.class), Mockito.eq(boardId));

        ResultActions resultActions = mockMvc.perform(put("/api/boards/{boardId}", boardId)
            .with(csrf()));

        resultActions.andExpect(status().isOk());
        verify(boardService, times(1)).isExitBoard(Mockito.any(HttpServletRequest.class), Mockito.eq(boardId));
    }


}
