package org.example.creww.notification.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.creww.post.entity.Post;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.user.repository.UserRepository;
import org.example.creww.userBoard.entity.UserBoard;
import org.example.creww.userBoard.repository.UserBoardRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationDomainService {

    private final UserBoardRepository userBoardRepository;
    private final NotificationService notificationService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void giveNotification(Long boardId, Long postId) {
        List<UserBoard> userList = userBoardRepository.findByBoardIdAndIsExitedFalse(boardId);
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 post"));
        String username = userRepository.findById(post.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 user")).getUsername();
        for (UserBoard user : userList) {
            notificationService.createNotification(user.getUserId(),
                username + "님이 " + post.getTitle() + " 게시글을 작성 하셨습니다.");
        }

    }
}
