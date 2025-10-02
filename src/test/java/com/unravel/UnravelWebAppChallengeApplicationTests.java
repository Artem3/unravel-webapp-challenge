package com.unravel;

import com.unravel.part1SessionManagement.SessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class UnravelWebAppChallengeApplicationTests {

    // Autowire the service bean to check if Spring loads it into the context
    @Autowired
    private SessionManager sessionManager;

    @Test
    void contextLoads() {
        // Assert that the autowired bean is not null, ensuring it was successfully loaded by Spring.
        assertNotNull(sessionManager, "SessionManager bean was not found in the application context.");
    }
}
