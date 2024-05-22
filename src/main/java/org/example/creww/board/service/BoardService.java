package org.example.creww.board.service;


import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.creww.board.dto.BoardRequest;
import org.example.creww.board.dto.BoardResponse;
import org.example.creww.board.entity.Board;
import org.example.creww.board.repository.BoardRepository;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.example.creww.userBoard.entity.UserBoard;
import org.example.creww.userBoard.repository.UserBoardRepository;
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
        //userIds.add(ownerId);
        for (Long userId : userIds) {
            if (userRepository.existsById(userId)) {
                UserBoard userBoard = new UserBoard(userId, board.getId());
                userBoardRepository.save(userBoard);
            }
        }
    }

    public List<BoardResponse> getBoard(String token) {
        if (token == null || !jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        List<UserBoard> userBoards = userBoardRepository.findByUserId(userId);
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
}
