package org.example.creww.post.service;


//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.notification.service.NotificationDomainService;
import org.example.creww.post.dto.PostRequest;
import org.example.creww.post.dto.PostResponse;
import org.example.creww.post.entity.Post;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final NotificationDomainService notificationDomainService;
    //post 생성
    @Transactional
    public PostResponse createPost(
        PostRequest postRequest,
        HttpServletRequest request,
        Long boardId
    ){
        //토큰 생성
        String token = jwtUtils.validateTokenOrThrow(request);
        //userId 추출해서 Long으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        String username = userRepository.findById(tokenUserId)
            .orElseThrow(() -> new ApplicationException("없는 유저", HttpStatus.NOT_FOUND))
            .getUsername();
        //post 객체 생성
        Post post = new Post(postRequest.getTitle(),postRequest.getContent(),tokenUserId,boardId);
        postRepository.save(post);
        // 저장 후 ID가 생성되었는지 확인
        if (post.getId() == null) {
            throw new ApplicationException("Failed to save the post, ID is null!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //post 객체 저장
        notificationDomainService.giveNotification(boardId,post.getId());
        //postResponse DTO 생성
        PostResponse postResponse = new PostResponse(post.getId(),post.getTitle(),post.getContent(),tokenUserId,username,post.getCreatedAt(),post.getViews());
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
         return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getUserId(), username, post.getCreatedAt(),post.getViews());
        });
    }
    // 포스트 단일 조회
    public PostResponse getPost(Long boardId, Long postId) {
        logger.info("getPost called with boardId: {} and postId: {}", boardId, postId);
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApplicationException("post 찾을 수 없음", HttpStatus.NOT_FOUND));
        if (!post.getBoardId().equals(boardId)) {
            throw new ApplicationException("올바른 요청이 아닙니다", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(post.getUserId())
            .orElseThrow(() -> new ApplicationException("없는 유저", HttpStatus.NOT_FOUND));
        post.setViews(post.getViews() + 1);
        postRepository.save(post);
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getUserId(), user.getUsername(), post.getCreatedAt(), post.getViews());
    }
    // post 삭제
    public void deletePost(Long postId, HttpServletRequest request) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApplicationException("게시물이 없습니다.", HttpStatus.NOT_FOUND));
        if (!post.getUserId().equals(tokenUserId)) {
            throw new ApplicationException("자신이 작성한 게시물글만 삭제 가능 합니다", HttpStatus.UNAUTHORIZED);
        }
        postRepository.delete(post);
    }

    // 업데이트
    @Transactional
    public void updatePost(Long boardId, Long postId, HttpServletRequest request, PostRequest postRequest) {
        String token = jwtUtils.validateTokenOrThrow(request);
        // userId 추출해서 Long으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApplicationException("없는 게시글", HttpStatus.NOT_FOUND));
        if (!boardId.equals(post.getBoardId())) {
            throw new ApplicationException("잘못된 게시글 입니다.", HttpStatus.BAD_REQUEST);
        }
        if (!tokenUserId.equals(post.getUserId())) {
            throw new ApplicationException("권한이 없습니다.", HttpStatus.UNAUTHORIZED);
        }
        post.updatePost(postRequest.getTitle(), postRequest.getContent(), tokenUserId, boardId);
        postRepository.save(post);
    }




}
