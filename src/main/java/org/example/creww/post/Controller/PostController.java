package org.example.creww.post.Controller;

//import jakarta.servlet.http.HttpServletRequest;
import io.swagger.annotations.ApiOperation;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.post.dto.PostRequest;
import org.example.creww.post.dto.PostResponse;
import org.example.creww.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
//@RequestMapping("/boards")
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    //포스트 작성
    @PostMapping("/{boardId}/posts")
    @ApiOperation(value = "게시글 생성", notes = "게시글을 생성 합니다.", tags = {"post-controller"})
    public ResponseEntity<PostResponse> createPost(
        @RequestBody PostRequest postRequest,
        @PathVariable Long boardId,
        HttpServletRequest request
    ) {
        PostResponse postResponse = postService.createPost(postRequest, request, boardId);
        return ResponseEntity.ok().body(postResponse);
    }
    //보드의 포스트 전부 조회
    @GetMapping("/{boardId}/posts")
    @ApiOperation(value = "게시글 전체 조회", notes = "게시글을 전체 조회 합니다.", tags = {"post-controller"})
    public ResponseEntity<Page<PostResponse>> getPosts(
        @PathVariable Long boardId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PostResponse> posts = postService.getPosts(boardId,page,size);
        return ResponseEntity.ok().body(posts);
    }
    //포스트 단일 조회
    @GetMapping("/{boardId}/posts/{postId}")
    @ApiOperation(value = "게시글 단일 조회", notes = "게시글을 단일 조회 합니다.", tags = {"post-controller"})
    public ResponseEntity<PostResponse> getPost(
        @PathVariable Long boardId,
        @PathVariable Long postId
    ){
        PostResponse postResponse = postService.getPost(boardId,postId);
        return ResponseEntity.ok().body(postResponse);
    }

    //포스트 수정
    @PutMapping("/{boardId}/posts/{postId}")
    @ApiOperation(value = "게시글 수정", notes = "게시글을 수정 합니다.", tags = {"post-controller"})
    public ResponseEntity<String> updatePost(
        @PathVariable Long boardId,
        @PathVariable Long postId,
        HttpServletRequest request,
        @RequestBody PostRequest postRequest
    ){
      postService.updatePost(boardId,postId,request,postRequest);
      return ResponseEntity.ok().body("게시글 수정이 완료 되었습니다.");
    }


    @DeleteMapping("/posts/{postId}")
    @ApiOperation(value = "게시글 삭제", notes = "게시글을 삭제 합니다.", tags = {"post-controller"})
    public ResponseEntity<String> deletePost(
        @PathVariable Long postId,
        HttpServletRequest request
    ) {
        postService.deletePost(postId, request);
        return ResponseEntity.ok().body("게시글이 성공적으로 삭제 되었습니다");
    }

}
