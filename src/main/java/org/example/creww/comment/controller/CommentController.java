package org.example.creww.comment.controller;


//import jakarta.servlet.http.HttpServletRequest;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.comment.dto.CommentRequest;
import org.example.creww.comment.dto.CommentResponse;
import org.example.creww.comment.service.CommentService;
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
//@RequestMapping("/boards/{boardId}/posts/{postId}/comments")
@RequestMapping("/api/boards/{boardId}/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;
    //댓글 생성
    @ApiOperation(value = "댓글 생성", notes = "댓글을 생성 합니다.", tags = {"comment-controller"})
    @PostMapping("")
    public ResponseEntity<CommentResponse> createComment(
        HttpServletRequest request,
        @PathVariable Long postId,
        @RequestBody CommentRequest commentRequest
    ) {
        CommentResponse commentResponse = commentService.createComment(request, postId,
            commentRequest);
        return ResponseEntity.ok().body(commentResponse);
    }
    //댓글 조회
    @ApiOperation(value = "댓글 조회", notes = "댓글을 조회 합니다.", tags = {"comment-controller"})
    @GetMapping("")
    public ResponseEntity<List<CommentResponse>> getComments(
        HttpServletRequest request,
        @PathVariable Long postId
    ) {
        List<CommentResponse> commentResponses = commentService.getComments(request, postId);
        return ResponseEntity.ok().body(commentResponses);
    }
    //댓글 삭제
    @ApiOperation(value = "댓글 삭제", notes = "댓글을 삭제 합니다.", tags = {"comment-controller"})
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
        HttpServletRequest request,
        @PathVariable Long postId,
        @PathVariable Long commentId) {
        commentService.deleteComment(request, commentId, postId);
        return ResponseEntity.ok().body("성공적으로 삭제 됐습니다.");
    }
    @ApiOperation(value = "댓글 수정", notes = "댓글을 수정 합니다.", tags = {"comment-controller"})
    @PutMapping("/{commentId}")
    public ResponseEntity<String> updateComment(
        @RequestBody CommentRequest commentRequest,
        HttpServletRequest request,
        @PathVariable Long postId,
        @PathVariable Long commentId){
        commentService.updateComment(request,postId,commentId,commentRequest);
        return ResponseEntity.ok().body("성공적으로 수정 됐습니다.");
    }





}
