package org.example.creww.userBoard.repository;

import java.util.List;
import org.example.creww.userBoard.entity.UserBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBoardRepository extends JpaRepository<UserBoard, Long> {
    List<UserBoard> findByBoardId(Long boardId);
    List<UserBoard> findByUserId(Long userId);
}
