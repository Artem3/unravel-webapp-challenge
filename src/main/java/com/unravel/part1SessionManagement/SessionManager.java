package com.unravel.part1SessionManagement;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SessionManager {

    public String login(String userId, HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "User already logged in.";
        }
        String sessionId = session.getId();
        session.setAttribute("userId", userId);
        log.info("User {} logged in with session ID: {}", userId, session.getId());

        return "Login successful. Session ID: " + sessionId;
    }

    public String logout(HttpSession session) {
        String sessionId = session.getId();
        session.invalidate();
        log.info("Logout successful for session ID: {}", sessionId);

        return "Logout successful.";
    }

    public String getSessionDetails(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            throw new CustomSessionException("Session not found");
        }
        return "Session ID for user " + userId + ": " + session.getId();
    }

    class CustomSessionException extends RuntimeException {
        public CustomSessionException(String message) {
            super(message);
        }
    }
}