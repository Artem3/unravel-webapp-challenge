package com.unravel.part2MemoryManagemet;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class MemoryManager {

    private static final Cache<String, byte[]> largeSessionData = Caffeine.newBuilder()
            .maximumSize(500)  // limit to 500 entries (adjust based on heap size)
            .expireAfterAccess(10, TimeUnit.SECONDS)  // Optionally. Evict after 10 sec inactivity
            .weakKeys()       // Optionally. Allow GC to collect if keys are unreferenced
            .build();

    public static void addSessionData(String sessionId) {
        largeSessionData.put(sessionId, new byte[10 * 1024 * 1024]);  // 10MB
    }

    // call this method when a session is invalidated or logged out to explicitly remove data
    public static void removeSessionData(String sessionId) {
        log.info("Removing session data for ID: {}", sessionId);
        largeSessionData.invalidate(sessionId);
    }
}