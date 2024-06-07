package org.example.creww.board.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.CrewwApplication;
import org.example.creww.board.dto.BoardAddUserRequest;
import org.example.creww.board.dto.BoardRequest;
import org.example.creww.board.dto.BoardResponse;
import org.example.creww.board.service.BoardService;
import org.example.creww.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    public String token;
    public Long userId;
    public BoardRequest boardRequest;

    @BeforeEach
    void setUp() {
        token = "mockToken";
        userId = 1L;
        boardRequest = new BoardRequest("테스트", Arrays.asList(2L, 3L), "test board");

        // mock jwt utils
        Mockito.when(jwtUtils.getTokenFromRequest(Mockito.any(HttpServletRequest.class)))
            .thenReturn(token); // getTokenFromRequest 실행되면 token 반환하도록 모킹
        Mockito.when(jwtUtils.validateToken(token))
            .thenReturn(true); // validateToken 실행되면 true 값을 반환하도록 모킹
        Mockito.when(jwtUtils.getUserIdFromToken(token))
            .thenReturn(String.valueOf(userId)); // 위에서 설정한 userId를 추출하도록 모킹
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 생성 테스트")
    void createBoard_Test() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(post("/api/boards/create") //post 요청 보냄
            .contentType(MediaType.APPLICATION_JSON)//요청 본문 json 으로 설정
            .content(objectMapper.writeValueAsString(boardRequest))//board request json 문자열로 변경
            .with(csrf())); //csrf 설정 통과

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(content().string("성공적으로 보드가 생성 되었습니다")); //응답검증
        MvcResult mvcResult = resultActions.andReturn(); //결과값 반환
        String responseBody = mvcResult.getResponse().getContentAsString(); //반환된 결과값을 문자열로 가져옴
        System.out.println("Response Body: " + responseBody);//출력
        verify(boardService, times(1)).createBoard(Mockito.eq(userId),
            Mockito.any(BoardRequest.class));//메소드가 1번 호출되었는지 검증
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 전체 조회 테스트")
    void getBoards_Test() throws Exception {
        // given
        List<BoardResponse> boardResponses = Arrays.asList( // 리스폰스 리스트 생성
            new BoardResponse("Board 1", 1L, "Description 1", "tester1"),
            new BoardResponse("Board 2", 2L, "Description 2", "tester2")
        );
        Mockito.when(boardService.getBoards(token))
            .thenReturn(boardResponses); // 보드서비스에서 리스폰스를 반환하도록 설정
        // when
        ResultActions resultActions = mockMvc.perform(get("/api/boards") //post 요청 보냄
            .contentType(MediaType.APPLICATION_JSON)//요청 본문 json 으로 설정
            .with(csrf())); //csrf 설정 통과

        // then
        for (int i = 0; i < boardResponses.size(); i++) { //jsonPath for 조건문 리스폰스 검증
            BoardResponse board = boardResponses.get(i);
            resultActions.andExpect(
                    jsonPath(String.format("$[%d].id", i), is(board.getId().intValue())))
                .andExpect(jsonPath(String.format("$[%d].boardName", i), is(board.getBoardName())))
                .andExpect(
                    jsonPath(String.format("$[%d].description", i), is(board.getDescription())));
        }

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody); //출력

        verify(boardService, times(1)).getBoards(token); //1번 실행됐나 검증
    }


    @Test
    @WithMockUser
    @DisplayName("게시판 단일 조회 테스트")
    void getBoard_Test() throws Exception {
        // given
        BoardResponse boardResponse = new BoardResponse("Board 1", 1L, "Description 1", "tester1");

        Mockito.when(boardService.getBoard(token, 1L))
            .thenReturn(boardResponse); // 보드서비스에서 리스폰스를 반환하도록 설정
        // when
        ResultActions resultActions = mockMvc.perform(get("/api/boards/1") //post 요청 보냄
            .contentType(MediaType.APPLICATION_JSON)//요청 본문 json 으로 설정
            .with(csrf())); //csrf 설정 통과

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.boardName").value("Board 1"))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.description").value("Description 1"))
            .andExpect(jsonPath("$.ownerName").value("tester1"));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody); //출력

        verify(boardService, times(1)).getBoard(token, 1L); //1번 실행됐나 검증
    }
    @Test
    @WithMockUser
    @DisplayName("게시판 유저 초대 테스트")
    void addUser_Test() throws Exception {
        // given
        List<Long> userIds = Arrays.asList(1L, 2L);
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(userIds);

        // mock boardService addUser method
        doNothing().when(boardService).addUser(token, boardAddUserRequest, 1L);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/boards/1/addUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardAddUserRequest)) // sending the correct JSON structure
            .with(csrf()));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(content().string("성공적으로 초대가 되었습니다"));
        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        System.out.println("Response Body: " + responseBody);
        verify(boardService, times(1)).addUser(Mockito.eq(token), Mockito.any(BoardAddUserRequest.class), Mockito.eq(1L));
    }
    @Test
    @WithMockUser
    @DisplayName("게시판 삭제 테스트")
    void deleteBoard_Test() throws Exception {
        // given
        Long boardId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/boards/{boardId}", boardId) // delete 요청 보냄
            .with(csrf())); // csrf 설정 통과

        // then
        resultActions.andExpect(status().isOk()); // 상태 코드 검증
        verify(boardService, times(1)).deleteBoard(token, boardId); // deleteBoard 메소드가 1번 호출되었는지 검증
    }
    @Test
    @WithMockUser
    @DisplayName("게시판 탈퇴 테스트")
    void exitBoard_Test() throws Exception {
        // given
        Long boardId = 1L;
        // when
        ResultActions resultActions = mockMvc.perform(put("/api/boards/{boardId}", boardId) // put 탈퇴 요청 보냄
            .with(csrf())); // csrf 설정 통과

        // then
        resultActions.andExpect(status().isOk()); // 상태 코드 검증
        verify(boardService, times(1)).exitBoard(token, boardId); // deleteBoard 메소드가 1번 호출되었는지 검증
    }
// ========================================= 실패 =================================================
@Test
@WithMockUser
@DisplayName("게시판 생성 실패 테스트 - 토큰 없음")
void createBoard_NoToken_Test() throws Exception {
    // given
    BoardRequest boardRequest = new BoardRequest("테스트", Arrays.asList(2L, 3L), "test board");

    Mockito.when(jwtUtils.getTokenFromRequest(Mockito.any(HttpServletRequest.class))).thenReturn(null); // 토큰 없음

    // when
    ResultActions resultActions = mockMvc.perform(post("/api/boards/create")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(boardRequest))
        .with(csrf()));

    // then
    resultActions.andExpect(status().isUnauthorized()); // UNAUTHORIZED 상태 코드 확인
}

    @Test
    @WithMockUser
    @DisplayName("게시판 생성 실패 테스트 - 유효하지 않은 토큰")
    void createBoard_InvalidToken_Test() throws Exception {
        // given
        BoardRequest boardRequest = new BoardRequest("테스트", Arrays.asList(2L, 3L), "test board");

        Mockito.when(jwtUtils.getTokenFromRequest(Mockito.any(HttpServletRequest.class))).thenReturn("invalidToken"); // 잘못된 토큰
        Mockito.when(jwtUtils.validateToken("invalidToken")).thenReturn(false); // 토큰 유효하지 않음

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/boards/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardRequest))
            .with(csrf()));

        // then
        resultActions.andExpect(status().isUnauthorized()); // UNAUTHORIZED 상태 코드 확인
    }

    @Test
    @WithMockUser
    @DisplayName("게시판 삭제 실패 테스트 - 게시판을 찾을 수 없음")
    void deleteBoard_NotFound_Test() throws Exception {
        // given
        String invalidToken = "invalidToken";
        Mockito.when(jwtUtils.getTokenFromRequest(Mockito.any(HttpServletRequest.class))).thenReturn(invalidToken);
        Mockito.doThrow(new IllegalArgumentException("존재하지 않는 게시판입니다")).when(boardService).deleteBoard(Mockito.eq(invalidToken), Mockito.anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/boards/1")
            .with(csrf()));

        // then
        resultActions.andExpect(status().isNotFound())
            .andExpect(content().string("존재하지 않는 게시판입니다")); // 예외 메시지 확인
    }
    @Test
    @WithMockUser
    @DisplayName("유저 초대 실패 테스트 - 토큰 없음")
    void addUser_NoToken_Test() throws Exception {
        // given
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(Arrays.asList(1L, 2L));
        Mockito.when(jwtUtils.getTokenFromRequest(Mockito.any(HttpServletRequest.class))).thenReturn(null); // 토큰 없음

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/boards/1/addUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardAddUserRequest))
            .with(csrf()));

        // then
        resultActions.andExpect(status().isUnauthorized()); // UNAUTHORIZED 상태 코드 확인
    }

    @Test
    @WithMockUser
    @DisplayName("유저 초대 실패 테스트 - 유효하지 않은 토큰")
    void addUser_InvalidToken_Test() throws Exception {
        // given
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(Arrays.asList(1L, 2L));
        Mockito.when(jwtUtils.getTokenFromRequest(Mockito.any(HttpServletRequest.class))).thenReturn("invalidToken"); // 잘못된 토큰
        Mockito.when(jwtUtils.validateToken("invalidToken")).thenReturn(false); // 토큰 유효하지 않음

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/boards/1/addUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardAddUserRequest))
            .with(csrf()));

        // then
        resultActions.andExpect(status().isUnauthorized()); // UNAUTHORIZED 상태 코드 확인
    }


}
