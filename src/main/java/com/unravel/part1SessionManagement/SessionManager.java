package com.unravel.part1SessionManagement;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SessionManager {

    public String login(String userId, HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "User already logged in.";
        }
        String sessionId = session.getId();
        session.setAttribute("userId", userId);
        return "Login successful. Session ID: " + sessionId;
    }

    public String logout(HttpSession session) {
        session.invalidate();
        return "Logout successful.";
    }

    public String getSessionDetails(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("Session not found");
        }
        return "Session ID for user " + userId + ": " + session.getId();
    }
}