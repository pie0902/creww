package org.example.creww.userBoard.repository;

import java.util.List;
import java.util.Optional;
import org.example.creww.userBoard.entity.UserBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.expression.spel.ast.OpPlus;

public interface UserBoardRepository extends JpaRepository<UserBoard, Long> {
    Optional<UserBoard> findByBoardIdAndUserId(Long boardId,Long userId);
    List<UserBoard> findByUserId(Long userId);
    //soft delete
    List<UserBoard> findByUserIdAndIsExitedFalse(Long userId);
    List<UserBoard> findByBoardIdAndIsExitedFalse(Long boardId);
    List<UserBoard> findByBoardId(Long boardId);
}
