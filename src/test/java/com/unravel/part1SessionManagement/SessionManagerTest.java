package com.unravel.part1SessionManagement;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionManagerTest {

    @InjectMocks
    private SessionManager sessionManager;

    @Mock
    private HttpSession mockSession;

    private static final String TEST_USER_ID = "testUser123";
    private static final String TEST_SESSION_ID = "a1b2c3d4e5f6";

    @BeforeEach
    void setUp() {
        // Initialize mocks before each test
        MockitoAnnotations.openMocks(this);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
    }

    // --- Tests for login() ---

    @Test
    void login_Successful() {
        // Setup: Session is initially empty
        when(mockSession.getAttribute("userId")).thenReturn(null);

        String result = sessionManager.login(TEST_USER_ID, mockSession);

        // Verify that setAttribute was called and the return message is correct
        assertTrue(result.contains("Login successful"));
        verify(mockSession, times(1)).setAttribute("userId", TEST_USER_ID);
    }

    @Test
    void login_AlreadyLoggedIn() {
        // Setup: Mock session already has a user ID
        when(mockSession.getAttribute("userId")).thenReturn(TEST_USER_ID);

        String result = sessionManager.login(TEST_USER_ID, mockSession);

        assertEquals("User already logged in.", result);
        // Verify setAttribute was NOT called
        verify(mockSession, never()).setAttribute(anyString(), any());
    }

    // --- Tests for logout() ---

    @Test
    void logout_Successful() {
        String result = sessionManager.logout(mockSession);

        // Verify session was invalidated
        verify(mockSession, times(1)).invalidate();
        assertEquals("Logout successful.", result);
    }

    // --- Tests for getSessionDetails() ---

    @Test
    void getSessionDetails_UserFound() {
        // Setup: Mock session contains a user ID
        when(mockSession.getAttribute("userId")).thenReturn(TEST_USER_ID);

        String result = sessionManager.getSessionDetails(mockSession);

        // Verify the result includes both user and session ID
        assertTrue(result.contains(TEST_USER_ID));
        assertTrue(result.contains(TEST_SESSION_ID));
    }

    @Test
    void getSessionDetails_SessionNotFound_ThrowsException() {
        // Setup: Mock session is empty
        when(mockSession.getAttribute("userId")).thenReturn(null);

        // Assert that a RuntimeException is thrown
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> sessionManager.getSessionDetails(mockSession)
        );

        assertEquals("Session not found", thrown.getMessage());
    }
}