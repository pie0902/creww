package org.example.creww.comment.service;

//import jakarta.servlet.http.HttpServletRequest;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.comment.dto.CommentRequest;
import org.example.creww.comment.dto.CommentResponse;
import org.example.creww.comment.entity.Comment;
import org.example.creww.comment.repository.CommentRepository;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.post.entity.Post;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.example.creww.userBoard.entity.UserBoard;
import org.example.creww.userBoard.repository.UserBoardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserBoardRepository userBoardRepository;

    public CommentResponse createComment(
        HttpServletRequest request,
        Long postId,
        CommentRequest commentRequest
    ) {
        String token = jwtUtils.validateTokenOrThrow(request);
        //userId 추출해서 Long으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        User user = userRepository.findById(tokenUserId)
            .orElseThrow(() -> new ApplicationException("없는 유저", HttpStatus.NOT_FOUND));
        Comment comment = new Comment(commentRequest.getContent(), user.getUsername(), postId,
            user.getId());
        commentRepository.save(comment);
        return new CommentResponse(comment.getId(), comment.getContent(),
            comment.getUsername());

    }

    public List<CommentResponse> getComments(HttpServletRequest request,Long boardId, Long postId) {
        String token = jwtUtils.validateTokenOrThrow(request);
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        // 사용자 보드 권한 확인
        userBoardRepository.findByBoardIdAndUserId(boardId, tokenUserId)
            .orElseThrow(() -> new ApplicationException("접근 권한이 없습니다", HttpStatus.FORBIDDEN));

        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream().map(comment -> {
            String username = userRepository.findById(comment.getUserId())
                .map(User::getUsername)
                .orElse("Unknown User");
            return new CommentResponse(comment.getId(), comment.getContent(), username);
        }).collect(Collectors.toList());
    }

    public void deleteComment(HttpServletRequest request, Long commentId,Long postId) {
        String token = jwtUtils.validateTokenOrThrow(request);
        //토큰 유저아이디 추출해서 Long type 으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        // comment 객체 생성
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new ApplicationException("댓글 없음",HttpStatus.NOT_FOUND));
        // 게시글 검증
        if(!postId.equals(comment.getPostId())) {
            throw new ApplicationException("해당 게시글 없음",HttpStatus.NOT_FOUND);
        }
        if(!tokenUserId.equals(comment.getUserId())) {
            throw new ApplicationException("권한 없음",HttpStatus.FORBIDDEN);
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
        String token = jwtUtils.validateTokenOrThrow(request);
        //userId 추출해서 Long으로 변경
        Long tokenUserId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        User user = userRepository.findById(tokenUserId)
            .orElseThrow(() -> new ApplicationException("없는 유저",HttpStatus.NOT_FOUND));
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new ApplicationException("없는 댓글",HttpStatus.NOT_FOUND));
        //게시글 아이디 검증
        if(!comment.getPostId().equals(postId)) {
            throw new ApplicationException(
                "잘못된 댓글 입니다",HttpStatus.BAD_REQUEST
            );
        }
        //게시글 유저 검증
        if(!comment.getUserId().equals(user.getId())){
            throw new ApplicationException("권한 없음",HttpStatus.FORBIDDEN);
        }
        comment.updateContent(commentRequest.getContent(),user.getUsername(),postId,user.getId());
        commentRepository.save(comment);

    }






}
