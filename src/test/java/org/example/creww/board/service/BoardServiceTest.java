package org.example.creww.board.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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
    private Board board;
    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "tester", "1234");
        ReflectionTestUtils.setField(user, "id", 1L);
        board = new Board("Test Board", "Test Description", user.getId());
        ReflectionTestUtils.setField(board, "id", 1L);

        List<Long> userIds = new ArrayList<>(Arrays.asList(1L, 2L));
        description = "test";
        boardRequest = new BoardRequest(board.getName(), userIds, description);
        token = "testToken";

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
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


}
