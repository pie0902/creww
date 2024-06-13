package org.example.creww.comment.service;

//import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.comment.dto.CommentRequest;
import org.example.creww.comment.dto.CommentResponse;
import org.example.creww.comment.entity.Comment;
import org.example.creww.comment.repository.CommentRepository;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public CommentResponse createComment(
        HttpServletRequest request,
        Long postId,
        CommentRequest commentRequest
    ) {
        String token = jwtUtils.getTokenFromRequest(request);
        //토큰 검증
        if (!jwtUtils.isTokenValid(token)) {
            throw new RuntimeException("토큰 이슈");
        }
        //userId 추출해서 Long으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        User user = userRepository.findById(tokenUserId)
            .orElseThrow(() -> new IllegalArgumentException("없는 유저"));
        Comment comment = new Comment(commentRequest.getContent(), user.getUsername(), postId,
            user.getId());
        commentRepository.save(comment);
        return new CommentResponse(comment.getId(), comment.getContent(),
            comment.getUsername());

    }

    public List<CommentResponse> getComments(HttpServletRequest request, Long postId) {
        String token = jwtUtils.getTokenFromRequest(request);
        // 토큰 검증
        if (!jwtUtils.isTokenValid(token)) {
            throw new RuntimeException("토큰 이슈");
        }
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream().map(comment -> {
            String username = userRepository.findById(comment.getUserId())
                .map(User::getUsername)
                .orElse("Unknown User");
            return new CommentResponse(comment.getId(), comment.getContent(), username);
        }).collect(Collectors.toList());
    }

    public void deleteComment(HttpServletRequest request, Long commentId,Long postId) {
        String token = jwtUtils.getTokenFromRequest(request);
        // 토큰 검증
        if (!jwtUtils.isTokenValid(token)) {
            throw new RuntimeException("토큰 이슈");
        }
        //토큰 유저아이디 추출해서 Long type 으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        // comment 객체 생성
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new IllegalArgumentException("댓글 없음"));
        // 게시글 검증
        if(!postId.equals(comment.getPostId())) {
            throw new RuntimeException("해당 게시글 없음");
        }
        if(!tokenUserId.equals(comment.getUserId())) {
            throw new RuntimeException("권한없음");
        }
        commentRepository.delete(comment);
    }
    @Transactional
    public void updateComment(
        HttpServletRequest request,
        Long postId,
        Long commentId,
        CommentRequest commentRequest
    ) {
        String token = jwtUtils.getTokenFromRequest(request);
        //토큰 검증
        if (!jwtUtils.isTokenValid(token)) {
            throw new RuntimeException("토큰 이슈");
        }
        //userId 추출해서 Long으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        User user = userRepository.findById(tokenUserId)
            .orElseThrow(() -> new IllegalArgumentException("없는 유저"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new IllegalArgumentException("없는 댓글"));
        //게시글 아이디 검증
        if(!comment.getPostId().equals(postId)) {
            throw new IllegalArgumentException(
                "잘못된 댓글 입니다"
            );
        }
        //게시글 유저 검증
        if(!comment.getUserId().equals(user.getId())){
            throw new RuntimeException("권한이 없습니다");
        }
        comment.updateContent(commentRequest.getContent(),user.getUsername(),postId,user.getId());
        commentRepository.save(comment);

    }





}
