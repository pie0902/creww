package org.example.creww;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;


//@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("test")
class CrewwApplicationTests {

    @Test
    void contextLoads() {
    }

}
