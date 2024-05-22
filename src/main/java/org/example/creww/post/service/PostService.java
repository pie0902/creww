package org.example.creww.post.service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.post.dto.PostRequest;
import org.example.creww.post.dto.PostResponse;
import org.example.creww.post.entity.Post;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    //post 생성
    @Transactional
    public PostResponse createPost(
        PostRequest postRequest,
        HttpServletRequest request,
        Long boardId
    ){
        //토큰 생성
        String token = jwtUtils.getTokenFromRequest(request);
        //토큰 검증
        if (!jwtUtils.isTokenValid(token)) {
            throw new RuntimeException("토큰 이슈");
        }
        //userId 추출해서 Long으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        String username = userRepository.findById(tokenUserId).orElseThrow(()->new IllegalArgumentException("없는 유저")).getUsername();
        //post 객체 생성
        Post post = new Post(postRequest.getTitle(),postRequest.getContent(),tokenUserId,boardId);
        //post 객체 저장
        postRepository.save(post);
        //postResponse DTO 생성
        PostResponse postResponse = new PostResponse(post.getId(),post.getTitle(),post.getContent(),tokenUserId,username,post.getCreatedAt());
        //postResponse DTO 반환
        return postResponse;
    }
// 포스트 전체 조회
    public Page<PostResponse> getPosts(Long boardId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByBoardId(boardId, pageable);
        return posts.map(post -> {
         String username = userRepository.findById(post.getUserId())
             .map(User::getUsername)
             .orElse("유저 없음");
         return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getUserId(), username, post.getCreatedAt());
        });
    }
    public PostResponse getPost(Long boardId,Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(()->new IllegalArgumentException("post 찾을 수 없음"));
        if(!post.getBoardId().equals(boardId)){
            throw new RuntimeException("올바른 요청이 아닙니다");
        }
        User user = userRepository.findById(post.getUserId()).orElseThrow(()->new IllegalArgumentException("없는 유저"));
        PostResponse postResponse = new PostResponse(post.getId(),post.getTitle(),post.getContent(),post.getUserId(),user.getUsername(),post.getCreatedAt());
        return postResponse;

    }
    //post 삭제
    public void deletePost(Long postId,HttpServletRequest request
    ) {
        String token = jwtUtils.getTokenFromRequest(request);
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        Post post = postRepository.findById(postId).orElseThrow(()->new RuntimeException("게시물이 없습니다."));
        if(!post.getId().equals(tokenUserId)) {
            throw new IllegalArgumentException("자신이 작성한 게시물글만 삭제 가능 합니다");
        }
        postRepository.delete(post);

    }



}
