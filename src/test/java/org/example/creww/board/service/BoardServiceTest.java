package org.example.creww.board.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.example.creww.board.dto.BoardAddUserRequest;
import org.example.creww.board.dto.BoardRequest;
import org.example.creww.board.dto.BoardResponse;
import org.example.creww.board.entity.Board;
import org.example.creww.board.repository.BoardRepository;
import org.example.creww.jwt.JwtUtils;
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
    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "tester", "1234");
        ReflectionTestUtils.setField(user, "id", 1L);
        user2 = new User("test2@test.com","tester2","1234");
        ReflectionTestUtils.setField(user2, "id", 2L);
        board = new Board("Test Board", "Test Description", user.getId());
        ReflectionTestUtils.setField(board, "id", 1L);
        userBoard = new UserBoard(user.getId(), board.getId());
        userIds = new ArrayList<>(Arrays.asList(1L, 2L));
        description = "test";
        boardRequest = new BoardRequest(board.getName(), userIds, description);
        token = "testToken";
    }
    @Test
    @DisplayName("보드 생성 테스트")
    void createBoard_test() {
        //given
        Long ownerId = 1L;
        when(userRepository.existsById(any(Long.class))).thenReturn(true);

        //when
        boardService.createBoard(ownerId, boardRequest);
        //then
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
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));

        // When
        List<BoardResponse> boardResponses = boardService.getBoards(token);

        // Then
        verify(jwtUtils, times(1)).validateToken(token);
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
    void getBoard(){
        //given
        Long boardId = board.getId();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(userRepository.findById(board.getOwnerId())).thenReturn(Optional.of(user));
        when(jwtUtils.validateToken(token)).thenReturn(true);
        //when
        BoardResponse boardResponse = boardService.getBoard(token,boardId);
        ReflectionTestUtils.setField(boardResponse, "id", 1L);
        //then
        verify(jwtUtils, times(1)).validateToken(token);
        verify(boardRepository, times(1)).findById(boardId);
        verify(userRepository, times(1)).findById(user.getId());

        assertEquals(1L,boardResponse.getId());
        assertEquals("Test Board", boardResponse.getBoardName());
        assertEquals("Test Description", boardResponse.getDescription());
        assertEquals("tester", boardResponse.getOwnerName());
    }
    @Test
    @DisplayName("보드 유저 추가 테스트")
    void addUser_test() {
        //given
        Long boardId = board.getId();
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(userRepository.findAllById(userIds)).thenReturn(Arrays.asList(user, user2));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        //when
        BoardAddUserRequest boardAddUserRequest = new BoardAddUserRequest(userIds);
        boardService.addUser(token, boardAddUserRequest, boardId);

        //Then
        verify(jwtUtils, times(1)).validateToken(token);
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
        when(userBoardRepository.findByBoardIdAndUserId(boardId, user.getId())).thenReturn(Optional.of(userBoard));
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        //when
        boardService.isExitBoard(token,boardId);
        //then
        verify(jwtUtils, times(1)).validateToken(token);
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
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));

        // when
        boardService.deleteBoard(token, boardId);

        // then
        verify(jwtUtils, times(1)).validateToken(token);
        verify(jwtUtils, times(1)).getUserIdFromToken(token);
        verify(boardRepository, times(1)).findById(boardId);
        verify(boardRepository, times(1)).delete(board);
    }

    @Test
    @DisplayName("보드 삭제 실패 테스트 - 보드 생성자가 아닌 사용자가 삭제하려는 경우")
    void deleteBoard_notOwner_fail_test() {
        // given
        Long boardId = board.getId();
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

        // 다른 사용자로 토큰 설정
        String otherToken = "otherToken";
        when(jwtUtils.validateToken(otherToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(otherToken)).thenReturn(String.valueOf(user2.getId()));

        // when, then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            boardService.deleteBoard(otherToken, boardId);
        });
        assertEquals("보드는 생성자만 삭제 가능합니다.", exception.getMessage());

        verify(jwtUtils, times(1)).validateToken(otherToken);
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
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));

        // when, then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            boardService.deleteBoard(token, boardId);
        });
        assertEquals("없는 보드", exception.getMessage());

        verify(jwtUtils, times(1)).validateToken(token);
        verify(jwtUtils, times(1)).getUserIdFromToken(token);
        verify(boardRepository, times(1)).findById(boardId);
        verify(boardRepository, never()).delete(any(Board.class));
    }
}
