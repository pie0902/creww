package org.example.creww.userBoard.repository.impl;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.example.creww.userBoard.entity.UserBoard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.example.creww.userBoard.repository.UserBoardRepositoryCustom;

public class UserBoardRepositoryImpl implements UserBoardRepositoryCustom {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserBoardRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void bulkInsert(List<UserBoard> userBoards) {
        String sql = "INSERT INTO user_board (user_id, board_id, is_exited) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UserBoard userBoard = userBoards.get(i);
                ps.setLong(1, userBoard.getUserId());
                ps.setLong(2, userBoard.getBoardId());
                ps.setBoolean(3, userBoard.isExited());
            }

            @Override
            public int getBatchSize() {
                return userBoards.size();
            }
        });
    }
}
