package org.example.creww.board.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.board.dto.BoardAddUserRequest;
import org.example.creww.board.dto.BoardRequest;
import org.example.creww.board.dto.BoardResponse;
import org.example.creww.board.entity.Board;
import org.example.creww.board.repository.BoardRepository;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.example.creww.userBoard.entity.UserBoard;
import org.example.creww.userBoard.repository.UserBoardRepository;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBoardRepository userBoardRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private BoardService boardService;

    private BoardRequest boardRequest;
    private String description;
    private String token;
    private User user;
    private User user2;
    private Board board;
    private List<Long> userIds;
    private UserBoard userBoard;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "tester", "1234");
        ReflectionTestUtils.setField(user, "id", 1L);
        user2 = new User("test2@test.com", "tester2", "1234");
        ReflectionTestUtils.setField(user2, "id", 2L);
        board = new Board("Test Board", "Test Description", user.getId());
        ReflectionTestUtils.setField(board, "id", 1L);
        userBoard = new UserBoard(user.getId(), board.getId());
        userIds = new ArrayList<>(Arrays.asList(1L, 2L));
        description = "test";
        boardRequest = new BoardRequest(board.getName(), userIds, description);
        token = "testToken";
        request = mock(HttpServletRequest.class);
    }

    @Test
    @DisplayName("보드 생성 테스트")
    void createBoard_test() {
        // Given
        String token = "validToken";
        Long ownerId = 123L;
        BoardRequest boardRequest = new BoardRequest("boardName", userIds, "description");
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(ownerId));
        when(userRepository.existsById(anyLong())).thenReturn(true); // 모든 사용자 ID가 존재한다고 가정

        // When
        boardService.createBoard(request, boardRequest);

        // Then
        verify(boardRepository).save(any(Board.class));
        verify(userBoardRepository, times(3)).save(any(UserBoard.class));
    }

    @Test
    @DisplayName("보드 조회 테스트")
    void getBoards_test() {
        // Given
        List<UserBoard> userBoards = Arrays.asList(new UserBoard(user.getId(), 1L));

        when(userBoardRepository.findByUserIdAndIsExitedFalse(user.getId())).thenReturn(userBoards);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));

        // When
        List<BoardResponse> boardResponses = boardService.getBoards(request);

        // Then
        verify(jwtUtils, times(1)).getUserIdFromToken(token);
        verify(userBoardRepository, times(1)).findByUserIdAndIsExitedFalse(user.getId());
        verify(boardRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(user.getId());

        assertEquals(1, boardResponses.size());
        BoardResponse boardResponse = boardResponses.get(0);
        assertEquals("Test Board", boardResponse.getBoardName());
        assertEquals("Test Description", boardResponse.getDescription());
        assertEquals("tester", boardResponse.getOwnerName());
    }


    @Test
    @DisplayName("보드 단일 조회 테스트")
    void getBoard() {
        //given
        Long boardId = board.getId();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(userRepository.findById(board.getOwnerId())).thenReturn(Optional.of(user));
        //when
        BoardResponse boardResponse = boardService.getBoard(boardId);
        ReflectionTestUtils.setField(boardResponse, "id", 1L);
        //then
        verify(boardRepository, times(1)).findById(boardId);
        verify(userRepository, times(1)).findById(user.getId());

        assertEquals(1L, boardResponse.getId());
        assertEquals("Test Board", boardResponse.getBoardName());
        assertEquals("Test Description", boardResponse.getDescription());
        assertEquals("tester", boardResponse.getOwnerName());
    }


    @Test
    @DisplayName("보드 유저 추가 테스트")
    void addUser_test() {
        //given
        Long boardId = 1L;
        List<Long> userIds = Arrays.asList(2L, 3L);
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(userIds);

        Mockito.when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        Mockito.when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        Mockito.when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        Mockito.when(userRepository.findAllById(userIds)).thenReturn(Arrays.asList(user, user2));

        //when
        boardService.addUser(request, boardAddUserRequest, boardId);

        //Then
        verify(jwtUtils, times(1)).validateTokenOrThrow(request);
        verify(jwtUtils, times(1)).getUserIdFromToken(token);
        verify(boardRepository, times(1)).findById(boardId);
        verify(userRepository, times(1)).findAllById(userIds);
        verify(userBoardRepository, times(2)).save(any(UserBoard.class));
    }


    @Test
    @DisplayName("보드 유저 나가기 테스트")
    void isExitBoard_test() {
        //given
        Long boardId = board.getId();
        when(userBoardRepository.findByBoardIdAndUserId(boardId, user.getId())).thenReturn(
            Optional.of(userBoard));
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        //when
        boardService.isExitBoard(request, boardId);
        //then
        verify(jwtUtils, times(1)).validateTokenOrThrow(request);
        verify(jwtUtils, times(1)).getUserIdFromToken(token);
        verify(userBoardRepository, times(1)).findByBoardIdAndUserId(eq(boardId), eq(user.getId()));
        verify(userBoardRepository, times(1)).save(any(UserBoard.class));
    }


    @Test
    @DisplayName("보드 삭제 테스트")
    void deleteBoard_test() {
        // given
        Long boardId = board.getId();
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        List<UserBoard> userBoards = Arrays.asList(userBoard);
        when(userBoardRepository.findByBoardId(boardId)).thenReturn(userBoards);

        // when
        boardService.deleteBoard(request, boardId);

        // then
        verify(jwtUtils, times(1)).validateTokenOrThrow(request);
        verify(jwtUtils, times(1)).getUserIdFromToken(token);
        verify(boardRepository, times(1)).findById(boardId);
        verify(boardRepository, times(1)).delete(board);
        verify(userBoardRepository, times(1)).delete(userBoard);
    }


//    ===============================================실패 =======================================

    @Test
    @DisplayName("보드 생성 유저 찾기 오류")
    void createBoard_error_test() {
        // Given
        String token = "mocked-token";
        HttpServletRequest request = mock(HttpServletRequest.class);
        List<Long> testUserIds = new ArrayList<>(Arrays.asList(2L));  // ArrayList로 변경
        BoardRequest boardRequest1 = new BoardRequest("test", testUserIds, "test");
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        Long ownerId = 1L;
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(ownerId.toString());

        // Mock the userRepository to return false for the user ID check
        when(userRepository.existsById(2L)).thenReturn(false);

        // When
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.createBoard(request, boardRequest1);
        });

        // Then
        assertEquals("User ID 2 does not exist", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("단일보드 조회 실패")
    public void getBoard_test() {
        //given
        Long boardId = 1L;
        Mockito.when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //when, then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.getBoard(boardId);
        });

        assertEquals("존재하지 않는 게시판", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("단일보드 조회 런타임 에러 테스트")
    public void getBoard_runtimeError_test() {
        //given
        Long boardId = 1L;
        Mockito.when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        //when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.getBoard(boardId);
        });
        assertEquals("존재하지 않는 게시판", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("보드 나가기 Invalid token fail")
    void isExitBoard_Invalid_token_fail() {
        //given
        Long boardId = board.getId();
        when(jwtUtils.validateTokenOrThrow(request)).thenThrow(
            new ApplicationException("Invalid token", HttpStatus.UNAUTHORIZED));
        //when&then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.isExitBoard(request, boardId);
        });
        assertEquals("Invalid token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    @DisplayName("보드 나가기 validateToken 오류 테스트")
    void isExitBoard_validateToken_error_test() {
        Long boardId = board.getId();
        when(jwtUtils.validateTokenOrThrow(request)).thenThrow(
            new ApplicationException("Invalid token", HttpStatus.UNAUTHORIZED));
        //when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.isExitBoard(request, boardId);
        });
        assertEquals("Invalid token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    @DisplayName("보드 삭제 실패 테스트 - 보드 생성자가 아닌 사용자가 삭제하려는 경우")
    void deleteBoard_notOwner_fail_test() {
        // given
        Long boardId = board.getId();
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        // 다른 사용자로 토큰 설정
        String otherToken = "otherToken";
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(otherToken);
        when(jwtUtils.getUserIdFromToken(otherToken)).thenReturn(String.valueOf(user2.getId()));

        // when, then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.deleteBoard(request, boardId);
        });
        assertEquals("보드는 생성자만 삭제 가능합니다.", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(jwtUtils, times(1)).validateTokenOrThrow(request);
        verify(jwtUtils, times(1)).getUserIdFromToken(otherToken);
        verify(boardRepository, times(1)).findById(boardId);
        verify(boardRepository, never()).delete(any(Board.class));
    }

    @Test
    @DisplayName("보드 삭제 실패 테스트 - 존재하지 않는 보드인 경우")
    void deleteBoard_notExist_fail_test() {
        // given
        Long boardId = 999L; // 존재하지 않는 보드 ID
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));

        // when, then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.deleteBoard(request, boardId);
        });
        assertEquals("없는 보드", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(jwtUtils, times(1)).validateTokenOrThrow(request);
        verify(jwtUtils, times(1)).getUserIdFromToken(token);
        verify(boardRepository, times(1)).findById(boardId);
        verify(boardRepository, never()).delete(any(Board.class));
    }

    @Test
    @DisplayName("보드 삭제 Invalid token fail")
    void deleteBoard_Invalid_token_fail() {
        //given
        when(jwtUtils.validateTokenOrThrow(request)).thenThrow(
            new ApplicationException("Invalid token", HttpStatus.UNAUTHORIZED));
        Long boardId = board.getId();
        //when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.deleteBoard(request, boardId);
        });
        assertEquals("Invalid token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    @DisplayName("보드 유저 추가 토큰 null 오류 테스트")
    void addUser_error_test() {
        //given
        Long boardId = 1L;
        List<Long> userIds = Arrays.asList(2L, 3L);
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(userIds);

        Mockito.when(jwtUtils.validateTokenOrThrow(request))
            .thenThrow(new ApplicationException("Invalid token", HttpStatus.UNAUTHORIZED));

        //when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.addUser(request, boardAddUserRequest, boardId);
        });
        assertEquals("Invalid token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    @DisplayName("보드 유저 추가 토큰 인증 오류 테스트")
    void addUser_error_test2() {
        //given
        Long boardId = 1L;
        List<Long> userIds = Arrays.asList(2L, 3L);
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(userIds);

        Mockito.when(jwtUtils.validateTokenOrThrow(request))
            .thenThrow(new ApplicationException("Invalid token", HttpStatus.UNAUTHORIZED));

        //when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.addUser(request, boardAddUserRequest, boardId);
        });
        assertEquals("Invalid token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    @DisplayName("보드 유저 추가 토큰 오류")
    void addUser_invalid_token_fail_test() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);

        // Mock behavior for JWT validation and user ID retrieval
        when(jwtUtils.validateTokenOrThrow(request)).thenThrow(
            new ApplicationException("Invalid token", HttpStatus.UNAUTHORIZED));

        List<Long> testIds = Arrays.asList(5L, 6L);
        BoardAddUserRequest boardAddUserRequest2 = new BoardAddUserRequest(testIds);

        //when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.addUser(request, boardAddUserRequest2, 1L);
        });

        assertEquals("Invalid token", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    @DisplayName("유저 추가 반장이 아닐때 오류")
    void addUser_owner_error_test() {
        when(jwtUtils.validateTokenOrThrow(request)).thenReturn(token);
        when(boardRepository.findById(user.getId())).thenReturn(Optional.of(board));
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf("2"));
        List<Long> userIds = Arrays.asList(5L, 6L);
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(userIds);


        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            boardService.addUser(request,boardAddUserRequest,board.getId());
        });

        assertEquals("방장만 회원 초대가 가능합니다.", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

    }


}
