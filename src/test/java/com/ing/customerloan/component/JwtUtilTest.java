package com.ing.customerloan.component;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtUtilTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Setting up mock JwtProperties
        String secretKey = "ZGR1eUxlTmRRZW9MN1FNcXF0SHBWbFhRcWZnUzFoY05hWXlFOW9wVThIdGMvNEs3T2U1Zkg2VkdzOUVMZEErMwo=";
        when(jwtProperties.getSecret()).thenReturn(secretKey);
        // 10 days
        long expirationTime = 864_000_000L;
        when(jwtProperties.getExpirationTime()).thenReturn(expirationTime);

        // Mocking UserDetails
        userDetails = User.builder()
                .username("testUser")
                .password("testPassword")
                .authorities("ROLE_USER")
                .build();
    }


    @Test
    void testValidateTokenValid() {
        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertTrue(isValid, "The token should be valid");
    }

    @Test
    void testValidateTokenInvalid() {
        String token = jwtUtil.generateToken(userDetails);

        // Creating a new user to simulate an invalid token
        UserDetails newUserDetails = User.builder()
                .username("newUser")
                .password("newPassword")
                .authorities("ROLE_USER")
                .build();

        boolean isValid = jwtUtil.validateToken(token, newUserDetails);

        assertFalse(isValid, "The token should be invalid for a different user");
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken(userDetails);

        String username = jwtUtil.extractUsername(token);

        assertEquals("testUser", username, "The extracted username should match the one in the token");
    }

    @Test
    void testExtractAllClaims() {
        String token = jwtUtil.generateToken(userDetails);

        Claims claims = jwtUtil.extractAllClaims(token);

        assertNotNull(claims, "Claims should not be null");
        assertEquals("testUser", claims.getSubject(), "The subject should match the username in the token");
    }
}
