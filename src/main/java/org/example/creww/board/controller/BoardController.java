package org.example.creww.board.controller;


//import jakarta.servlet.http.HttpServletRequest;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.board.dto.BoardAddUserRequest;
import org.example.creww.board.dto.BoardRequest;
import org.example.creww.board.dto.BoardResponse;
import org.example.creww.board.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
//@RequestMapping("/boards")
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;
    @ApiOperation(value = "게시판 생성", notes = "게시판을 생성합니다.", tags = {"board-controller"})
    @PostMapping("/create")
    public ResponseEntity<String> createBoard(HttpServletRequest request, @RequestBody BoardRequest boardRequest) {
        boardService.createBoard(request,boardRequest);
        return ResponseEntity.ok().body("성공적으로 보드가 생성 되었습니다");
    }
    @ApiOperation(value = "게시판 전체 조회", notes = "게시판을 전체 조회 합니다.", tags = {"board-controller"})
    @GetMapping("")
    public ResponseEntity<List<BoardResponse>> getBoards(HttpServletRequest request) {
        List<BoardResponse> boards = boardService.getBoards(request);
        return ResponseEntity.ok().body(boards);
    }
    @ApiOperation(value = "게시판 단일 조회", notes = "게시판을 단일 조회 합니다.", tags = {"board-controller"})
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(
        @PathVariable Long boardId
    ) {
        BoardResponse boardResponse = boardService.getBoard(boardId);
        return ResponseEntity.ok().body(boardResponse);
    }

    //방장 유저 초대
    @ApiOperation(value = "게시판 유저 초대", notes = "게시판에 유저를 초대합니다.", tags = {"board-controller"})
    @PostMapping("/{boardId}/addUser")
    public ResponseEntity<String> addUser(
        HttpServletRequest request,
        @RequestBody BoardAddUserRequest boardAddUserRequest,
        @PathVariable Long boardId
    ) {
        boardService.addUser(request,boardAddUserRequest,boardId);
        return ResponseEntity.ok().body("성공적으로 초대가 되었습니다");
    }

    @PutMapping("/{boardId}")
    @ApiOperation(value = "게시판 탈퇴", notes = "게시판을 탈퇴 합니다.", tags = {"board-controller"})
    public ResponseEntity<String> exitBoard(
        @PathVariable Long boardId,
        HttpServletRequest request
    ){
        boardService.isExitBoard(request,boardId);
        return ResponseEntity.ok().body("게시판을 탈퇴 했습니다.");
    }






    @DeleteMapping("/{boardId}")
    @ApiOperation(value = "게시판 삭제", notes = "게시판을 삭제 합니다.", tags = {"board-controller"})
    public ResponseEntity<String> deletePost(
        @PathVariable Long boardId,
        HttpServletRequest request
    ) {
        try {
            boardService.deleteBoard(request, boardId);
            return ResponseEntity.ok().body("게시글이 성공적으로 삭제 되었습니다");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}


