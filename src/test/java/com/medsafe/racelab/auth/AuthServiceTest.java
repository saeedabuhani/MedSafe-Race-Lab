package com.medsafe.racelab.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    private final AuthService authService = new AuthService();

    @Test
    void acceptsTheDocumentedDemoCredentials() {
        assertTrue(authService.authenticate(
                AuthService.DEMO_USERNAME,
                AuthService.DEMO_PASSWORD.toCharArray()));
    }

    @Test
    void rejectsIncorrectCredentials() {
        assertFalse(authService.authenticate("student", "wrong".toCharArray()));
        assertFalse(authService.authenticate("unknown", "MedSafe2026!".toCharArray()));
    }

    @Test
    void clearsTheCallerPasswordArrayAfterVerification() {
        char[] password = AuthService.DEMO_PASSWORD.toCharArray();
        authService.authenticate(AuthService.DEMO_USERNAME, password);

        for (char character : password) {
            assertTrue(character == '\0');
        }
    }
}
