package org.example.creww.userBoard.repository;

import java.util.List;
import org.example.creww.userBoard.entity.UserBoard;

public interface UserBoardRepositoryCustom {
    void bulkInsert(List<UserBoard> userBoards);
}
