package org.example.creww.userBoard.repository;

import java.util.List;
import java.util.Optional;
import org.example.creww.board.dto.BoardDetailsDTO;
import org.example.creww.userBoard.entity.UserBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.expression.spel.ast.OpPlus;

public interface UserBoardRepository extends JpaRepository<UserBoard, Long> {

    Optional<UserBoard> findByBoardIdAndUserId(Long boardId, Long userId);

    //soft delete
    List<UserBoard> findByUserIdAndIsExitedFalse(Long userId);

    List<UserBoard> findByBoardIdAndIsExitedFalse(Long boardId);

    List<UserBoard> findByBoardId(Long boardId);

    @Query(
        "SELECT new org.example.creww.board.dto.BoardDetailsDTO(ub.boardId, b.name, b.description, u.username) "
            +
            "FROM UserBoard ub " +
            "JOIN Board b ON ub.boardId = b.id " +
            "JOIN User u ON b.ownerId = u.id " +
            "WHERE ub.userId = :userId AND ub.isExited = false")
    List<BoardDetailsDTO> findBoardsWithOwnerByUserId(@Param("userId") Long userId);

    @Query("SELECT ub.userId FROM UserBoard ub WHERE ub.boardId = :boardId AND ub.isExited = false")
    List<Long> findUserIdsByBoardIdAndIsExitedFalse(@Param("boardId") Long boardId);

}
