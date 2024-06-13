package org.example.creww;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        // 데이터베이스 연결 정보 출력
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            System.out.println("Connected to: " + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("데이터베이스 URL 확인 테스트")
    void testDatabaseUrl() {
        // 데이터베이스 연결 정보 검증
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            assertEquals("jdbc:mysql://localhost:3306/testdb", url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
