package org.example.creww.board.controller;


import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.creww.board.dto.BoardRequest;
import org.example.creww.board.dto.BoardResponse;
import org.example.creww.board.service.BoardService;
import org.example.creww.jwt.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;
    private final JwtUtils jwtUtils;
    @PostMapping("/create")
    public ResponseEntity<String> createBoard(HttpServletRequest request, @RequestBody BoardRequest boardRequest) {
        String token = jwtUtils.getTokenFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Long creatorId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        boardService.createBoard(creatorId,boardRequest);
        return ResponseEntity.ok().body("성공적으로 보드가 생성 되었습니다");
    }
    @GetMapping("")
    public ResponseEntity<List<BoardResponse>> getBoards(HttpServletRequest request) {
        String token = jwtUtils.getTokenFromRequest(request);
        List<BoardResponse> boards = boardService.getBoard(token);
        return ResponseEntity.ok().body(boards);
    }
}
