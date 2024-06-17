package org.example.creww.board.service;


import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final UserBoardRepository userBoardRepository;
    private final JwtUtils jwtUtils;

    @Transactional
    public void createBoard(HttpServletRequest request, BoardRequest req) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long ownerId = Long.parseLong(jwtUtils.getUserIdFromToken(token));

        // Create and save the board
        Board board = new Board(req.getBoardName(), req.getDescription(), ownerId);
        boardRepository.save(board);

        // Collect user IDs, including the owner
        List<Long> userIds = req.getUserIds();
        userIds.add(ownerId);

        // Add users to the board
        for (Long userId : userIds) {
            if (userRepository.existsById(userId)) {
                UserBoard userBoard = new UserBoard(userId, board.getId());
                userBoardRepository.save(userBoard);
            } else {
                throw new ApplicationException("User ID " + userId + " does not exist", HttpStatus.NOT_FOUND);
            }
        }
    }

    public List<BoardResponse> getBoards(HttpServletRequest request) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        List<UserBoard> userBoards = userBoardRepository.findByUserIdAndIsExitedFalse(userId);
        return userBoards.stream()
            .map(userBoard -> {
                Board board = boardRepository.findById(userBoard.getBoardId())
                    .orElseThrow(() -> new ApplicationException("Board does not exist", HttpStatus.NOT_FOUND));
                User user = userRepository.findById(board.getOwnerId())
                    .orElseThrow(() -> new ApplicationException("없는 유저", HttpStatus.NOT_FOUND));
                String ownerName = user.getUsername();
                return new BoardResponse(board.getName(), board.getId(), board.getDescription(), ownerName);
            })
            .collect(Collectors.toList());
    }

    public BoardResponse getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new ApplicationException("존재하지 않는 게시판", HttpStatus.NOT_FOUND));
        User user = userRepository.findById(board.getOwnerId())
            .orElseThrow(() -> new ApplicationException("존재하지 않는 유저", HttpStatus.NOT_FOUND));
        return new BoardResponse(board.getName(), board.getId(), board.getDescription(), user.getUsername());
    }

    @Transactional
    public void addUser(HttpServletRequest request, BoardAddUserRequest boardAddUserRequest, Long boardId) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new ApplicationException("존재하지 않는 보드", HttpStatus.NOT_FOUND));
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        if (!board.getOwnerId().equals(userId)) {
            throw new ApplicationException("방장만 회원 초대가 가능합니다.", HttpStatus.FORBIDDEN);
        }

        List<Long> userIds = boardAddUserRequest.getUserIds();
        List<Long> existingUserIds = userRepository.findAllById(userIds).stream().map(User::getId)
            .collect(Collectors.toList());
        for (Long id : existingUserIds) {
            UserBoard userBoard = new UserBoard(id, board.getId());
            userBoardRepository.save(userBoard);
        }
    }

    public void isExitBoard(HttpServletRequest request, Long boardId) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        UserBoard userBoard = userBoardRepository.findByBoardIdAndUserId(boardId, userId)
            .orElseThrow(() -> new ApplicationException("없는 테이블", HttpStatus.NOT_FOUND));
        userBoard.setExited();
        userBoardRepository.save(userBoard);
    }

    @Transactional
    public void deleteBoard(HttpServletRequest request, Long boardId) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new ApplicationException("없는 보드", HttpStatus.NOT_FOUND));
        if (!board.getOwnerId().equals(userId)) {
            throw new ApplicationException("보드는 생성자만 삭제 가능합니다.", HttpStatus.FORBIDDEN);
        }
        boardRepository.delete(board);
        List<UserBoard> userBoards = userBoardRepository.findByBoardId(boardId);
        for (UserBoard userBoard : userBoards) {
            userBoardRepository.delete(userBoard);
        }
    }
}
