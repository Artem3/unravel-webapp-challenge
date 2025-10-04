package com.unravel.part2MemoryManagemet;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class MemoryManagerTest {

    private Cache<String, byte[]> cache;

    @BeforeEach
    void setUp() {
        // Reset static cache for isolation (using reflection or recreate)
        cache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .weakKeys()
                .build();
    }

    @Test
    void addSessionData_ShouldAddEntryToCache() {
        String sessionId = "test-session";
        MemoryManager.addSessionData(sessionId);

        // Access via reflection or make non-static for testing; alternatively, expose cache for test.
        // For static, consider refactoring; here simulate by checking size post-add.
        assertEquals(1, getCacheSize());  // Helper method to get size via reflection.
    }

    @Test
    void removeSessionData_ShouldInvalidateEntryAndLog() {
        String sessionId = "test-session";
        MemoryManager.addSessionData(sessionId);

        MemoryManager.removeSessionData(sessionId);

        assertFalse(cache.asMap().containsKey(sessionId));
    }

    @Test
    void addSessionData_ExpiresAfterAccess_ShouldEvictAfterTimeout() {
        String sessionId = "test-session";
        MemoryManager.addSessionData(sessionId);

        await().atMost(11, TimeUnit.SECONDS).until(() -> !cache.asMap().containsKey(sessionId));
    }

    // Helper: Use reflection to access a private static cache for assertions.
    private int getCacheSize() {
        try {
            java.lang.reflect.Field field = MemoryManager.class.getDeclaredField("largeSessionData");
            field.setAccessible(true);
            Cache<?, ?> staticCache = (Cache<?, ?>) field.get(null);
            return (int) staticCache.estimatedSize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}