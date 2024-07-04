package org.example.creww.notification.service;


import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.repository.NotificationRepository;
import org.example.creww.post.dto.PostWithUser;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.userBoard.repository.UserBoardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationDomainService {

    private final UserBoardRepository userBoardRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;

    //알림생성 & 알림 주기
    @Transactional
    public void giveNotification(Long boardId, Long postId) {
        PostWithUser postWithUser = postRepository.findPostWithUserById(postId)
            .orElseThrow(() -> new ApplicationException("게시글 없음", HttpStatus.NOT_FOUND));

        String message =
            postWithUser.getUsername() + "님이 " + postWithUser.getPostTitle() + " 게시글을 작성하셨습니다.";

        List<Long> userIds = userBoardRepository.findUserIdsByBoardIdAndIsExitedFalse(boardId);

        if (userIds != null) {
            List<Notification> notifications = userIds.stream()
                .map(userId -> new Notification(userId, message))
                .collect(Collectors.toList());

            if (notificationRepository != null) {
                notificationRepository.bulkInsert(notifications);
            } else {
                throw new ApplicationException("notificationRepository is null",
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
