package org.example.creww.notification.service;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.notification.entity.Notification;
import org.example.creww.notification.repository.NotificationRepository;
import org.example.creww.post.dto.PostWithUser;
import org.example.creww.post.repository.PostRepository;
import org.example.creww.userBoard.repository.UserBoardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationDomainService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>(); // 동시성 컬렉션 인스턴스 (멀티스레드 환경에서 안전하게 사용할 수 있는 Map 구현체)
    private final UserBoardRepository userBoardRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void giveNotification(Long boardId, Long postId) {
        PostWithUser postWithUser = postRepository.findPostWithUserById(postId)
            .orElseThrow(() -> new ApplicationException("게시글 없음", HttpStatus.NOT_FOUND));
        String message = postWithUser.getUsername() + "님이 " + postWithUser.getPostTitle() + " 게시글을 작성하셨습니다.";
        List<Long> userIds = userBoardRepository.findUserIdsByBoardIdAndIsExitedFalse(boardId);

        if (userIds != null) {
            // 알림 객체를 생성
            List<Notification> notifications = userIds.stream()
                .map(userId -> new Notification(userId, message))
                .collect(Collectors.toList());

            if (notificationRepository != null) {
                // 알림을 한 번에 DB에 저장
                notificationRepository.bulkInsert(notifications);
                // Map<userId,알림개수>
                Map<Long, Long> userNotificationCounts = notificationRepository.countNewNotificationsByUserIds(userIds);
                notifications.forEach(notification -> {
                    sendNotification(notification); //현재 알림 전송
                    sendNotificationCount(notification.getUserId(), userNotificationCounts.get(notification.getUserId())); // 현재 알림 개수 전송
                });
            } else {
                throw new ApplicationException("notificationRepository is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }


    public void sendNotification(Notification notification) {
        // 주어진 notification의 userId에 해당하는 SseEmitter를 맵에서 가져온다.
        SseEmitter emitter = emitters.get(notification.getUserId());
        //emitter가 존재하는 경우 (사용자가 SSE 연결을 열어두는 경우) 실행한다.
        if (emitter != null) {
            try {
                // SseEmitter를 사용하여 실제로 이벤트를 전송한다.
                emitter.send(SseEmitter.event()
                    .id(notification.getId().toString()) //이벤트의 고유 ID 설정
                    .name("New_Post") //이벤트의 이름 설정( 클라이언트에서 이 이름으로 이벤트를 구분)
                    .data(notification.getMessage())); //실제 전송할 데이터 (알림 메시지)
            } catch (IOException e) {
                //전송 중 오류 발생하면 해당 emitter를 맵에서 제거한다.
                emitters.remove(notification.getUserId());
            }
        }
    }
    private void sendNotificationCount(Long userId, Long count) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification_count")
                    .data(count));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
    public SseEmitter subscribe(@RequestParam Long userId){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);//SseEmitter 객체 생성, Long.MAX_VALUE는 연결 timeout 시간을 최대로 설정
        emitters.put(userId,emitter); //새 사용자 연결 추가
        emitter.onCompletion(() -> emitters.remove(userId));// 클라이언트와 연결이 완료되면 실행될 콜백 설정 (클라이언트가 연결을 종료하면 userId의 emitter를 제거
        emitter.onTimeout(() -> emitters.remove(userId));// 연결이 timeout 되면 실행될 콜백 설정 (마찬가지로 userId의 emitter를 제거)
        return emitter;
    }


}

