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
import org.example.creww.jwt.JwtUtils;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.example.creww.userBoard.entity.UserBoard;
import org.example.creww.userBoard.repository.UserBoardRepository;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
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
    public void createBoard(Long ownerId, BoardRequest req) {
        Board board = new Board(req.getBoardName(), req.getDescription(), ownerId);
        boardRepository.save(board);
        List<Long> userIds = req.getUserIds();
        userIds.add(ownerId);
        for (Long userId : userIds) {
            if (userRepository.existsById(userId)) {
                UserBoard userBoard = new UserBoard(userId, board.getId());
                userBoardRepository.save(userBoard);
            }
        }
    }

    public List<BoardResponse> getBoards(
        String token
    ) {
        if (token == null || !jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        List<UserBoard> userBoards = userBoardRepository.findByUserIdAndIsExitedFalse(userId);
        return userBoards.stream()
            .map(userBoard -> {
                Board board = boardRepository.findById(userBoard.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("Board does not exist"));
                User user = userRepository.findById(board.getOwnerId()).orElseThrow(()->new IllegalArgumentException("없는 유저"));
                String ownerName = user.getUsername();
                return new BoardResponse(board.getName(), board.getId(), board.getDescription(),ownerName);
            })
            .collect(Collectors.toList());
    }

    public BoardResponse getBoard(
        String token,
        Long boardId
    ) {
        if (token == null || !jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
<<<<<<< HEAD
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판"));
        User user = userRepository.findById(board.getOwnerId())
            .orElseThrow(() -> new IllegalIdentifierException("존재하지 않는 유저"));
        BoardResponse boardResponse = new BoardResponse(board.getName(), board.getId(),
            board.getDescription(), user.getUsername());
        return boardResponse;
    }
    @Transactional
    public void addUser(String token, BoardAddUserRequest boardAddUserRequest, Long boardId) {
        if (token == null || !jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 보드"));
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));

        if (!board.getOwnerId().equals(userId)) {
            throw new IllegalIdentifierException("방장만 회원 초대가 가능합니다.");
        }

        List<Long> userIds = boardAddUserRequest.getUserIds();
        List<Long> existingUserIds = userRepository.findAllById(userIds).stream().map(User::getId).collect(Collectors.toList());

        for (Long id : existingUserIds) {
            UserBoard userBoard = new UserBoard(id, board.getId());
            userBoardRepository.save(userBoard);
        }
=======
        Board board = boardRepository.findById(boardId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시판"));

        User user = userRepository.findById(board.getOwnerId()).orElseThrow(()->new IllegalIdentifierException("존재하지 않는 유저"));
        BoardResponse boardResponse = new BoardResponse(board.getName(),board.getId(),board.getDescription(),user.getUsername());
        return boardResponse;
    }

    //게시판 나가기
    public void exitBoard(String token, Long boardId) {
        if (token == null || !jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        UserBoard userBoard = userBoardRepository.findByBoardIdAndUserId(boardId,userId).orElseThrow(()->new IllegalArgumentException("없는 테이블"));
        userBoard.setExited();
>>>>>>> cb59ded4b93c7b825b029f76b5c7bf4c43d0cd03
    }



<<<<<<< HEAD
=======
    //관리자 게시판 삭제
    public void deleteBoard(String token,Long boardId) {
        if (token == null || !jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        Board board = boardRepository.findById(boardId).orElseThrow(()->new IllegalArgumentException("없는 보드"));

        if(!board.getOwnerId().equals(userId)) {
            throw new RuntimeException("보드는 생성자만 삭제 가능합니다.");
        }
        boardRepository.delete(board);
    }

>>>>>>> cb59ded4b93c7b825b029f76b5c7bf4c43d0cd03

}
