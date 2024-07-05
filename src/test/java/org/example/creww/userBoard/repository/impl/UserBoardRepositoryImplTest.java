package org.example.creww.userBoard.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.example.creww.userBoard.entity.UserBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserBoardRepositoryImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserBoardRepositoryImpl userBoardRepository;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<BatchPreparedStatementSetter> batchPreparedStatementSetterCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("bulkInsert 호출 시 batchUpdate가 호출되는지 테스트")
    void bulkInsert_test() {
        // Given
        UserBoard userBoard1 = new UserBoard(1L, 1L);
        UserBoard userBoard2 = new UserBoard(2L, 1L);
        List<UserBoard> userBoards = Arrays.asList(userBoard1, userBoard2);

        // When
        userBoardRepository.bulkInsert(userBoards);

        // Then
        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), batchPreparedStatementSetterCaptor.capture());

        String expectedSql = "INSERT INTO user_board (user_id, board_id, is_exited) VALUES (?, ?, ?)";
        assertEquals(expectedSql, sqlCaptor.getValue());

        BatchPreparedStatementSetter setter = batchPreparedStatementSetterCaptor.getValue();
        assertNotNull(setter);

        assertEquals(userBoards.size(), setter.getBatchSize());

        try {
            PreparedStatement ps = mock(PreparedStatement.class);
            setter.setValues(ps, 0);
            setter.setValues(ps, 1);

            InOrder inOrder = inOrder(ps);
            inOrder.verify(ps).setLong(1, userBoards.get(0).getUserId());
            inOrder.verify(ps).setLong(2, userBoards.get(0).getBoardId());
            inOrder.verify(ps).setBoolean(3, userBoards.get(0).isExited());

            inOrder.verify(ps).setLong(1, userBoards.get(1).getUserId());
            inOrder.verify(ps).setLong(2, userBoards.get(1).getBoardId());
            inOrder.verify(ps).setBoolean(3, userBoards.get(1).isExited());
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
    }
}
