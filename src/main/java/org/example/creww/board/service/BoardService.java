package org.example.creww.board.service;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.board.dto.BoardAddUserRequest;
import org.example.creww.board.dto.BoardDetailsDTO;
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
    //
    @Transactional
    public void createBoard(HttpServletRequest request, BoardRequest req) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long ownerId = Long.parseLong(jwtUtils.getUserIdFromToken(token));

        // Create and save the board
        Board board = new Board(req.getBoardName(), req.getDescription(), ownerId);
        boardRepository.save(board);

        // Collect user IDs, including the owner
        List<Long> userIds = new ArrayList<>(req.getUserIds());
        userIds.add(ownerId);
        // 모든 유저 체크
        List<Long> existingUserIds = userRepository.findAllUserIdsByIdIn(userIds);
        if(existingUserIds.size() != userIds.size()) {
            Set<Long> nonExistingUserIds = new HashSet<>(userIds);
            nonExistingUserIds.removeAll(existingUserIds);
            throw new ApplicationException("User IDs" + nonExistingUserIds + "do not exist",
                HttpStatus.NOT_FOUND);
        }
        // Create UserBoard objects
        List<UserBoard> userBoards = userIds.stream()
            .map(userId -> new UserBoard(userId, board.getId()))
            .collect(Collectors.toList());

        // Bulk insert UserBoard entities
        userBoardRepository.bulkInsert(userBoards);

    }

    public List<BoardResponse> getBoards(HttpServletRequest request) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));

        List<BoardDetailsDTO> boardDetails = userBoardRepository.findBoardsWithOwnerByUserId(userId);

        return boardDetails.stream()
            .map(dto -> new BoardResponse(dto.getName(), dto.getBoardId(), dto.getDescription(), dto.getOwnerName()))
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
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new ApplicationException("존재하지 않는 보드입니다.",HttpStatus.NOT_FOUND));
        if (!board.getOwnerId().equals(userId)) {
            throw new ApplicationException("방장만 회원 초대가 가능합니다.",HttpStatus.FORBIDDEN);
        }
        List<Long> requestedUserIds = boardAddUserRequest.getUserIds();
        List<User> existingUsers = userRepository.findAllById(requestedUserIds);
        List<Long> existingUserIds = existingUsers.stream().map(User::getId).collect(Collectors.toList());


        // 존재하지 않는 사용자 ID 확인
        List<Long> nonExistingUserIds = requestedUserIds.stream()
            .filter(id -> !existingUserIds.contains(id))
            .collect(Collectors.toList());
        if (!nonExistingUserIds.isEmpty()) {
            throw new ApplicationException("존재하지 않는 사용자: " + nonExistingUserIds,HttpStatus.NOT_FOUND);
        }

        // 이미 초대된 사용자 제외
        List<Long> alreadyInvitedUserIds = userBoardRepository.findByBoardId(boardId)
            .stream().map(UserBoard::getUserId).collect(Collectors.toList());
        existingUserIds.removeAll(alreadyInvitedUserIds);
        // 초대할 사용자가 있으면 초대함
        if (!existingUserIds.isEmpty()) {
            List<UserBoard> userBoards = existingUserIds.stream()
                .map(id -> new UserBoard(id, boardId))
                .collect(Collectors.toList());
            userBoardRepository.saveAll(userBoards);
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
