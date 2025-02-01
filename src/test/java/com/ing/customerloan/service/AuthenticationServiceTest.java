package com.ing.customerloan.service;

import com.ing.customerloan.component.JwtUtil;
import com.ing.customerloan.dto.AuthenticationRequestDTO;
import com.ing.customerloan.dto.AuthenticationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthenticationRequestDTO authenticationRequestDTO;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        authenticationRequestDTO = new AuthenticationRequestDTO();
        authenticationRequestDTO.setUsername("testUser");
        authenticationRequestDTO.setPassword("testPassword");

        userDetails = mock(UserDetails.class);
    }

    @Test
    void testCreateAuthenticationToken_Success() {
        // Mock authenticationManager.authenticate
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Mock userDetailsService.loadUserByUsername
        when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(userDetails);

        // Mock jwtUtil.generateToken
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("testToken");

        // Call the method under test
        AuthenticationResponseDTO result = authenticationService.createAuthenticationToken(authenticationRequestDTO);

        // Verify the results
        assertNotNull(result);
        assertEquals("testToken", result.getToken());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(any(String.class));
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    void testCreateAuthenticationToken_AuthenticationFailure() {
        // Mock authenticationManager.authenticate to throw an exception
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Call the method under test and expect an exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authenticationService.createAuthenticationToken(authenticationRequestDTO));

        // Verify the exception message
        assertEquals("Authentication failed", exception.getMessage());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(any(String.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void testCreateAuthenticationToken_UserDetailsNotFound() {
        // Mock authenticationManager.authenticate
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Mock userDetailsService.loadUserByUsername to throw an exception
        when(userDetailsService.loadUserByUsername(any(String.class))).thenThrow(new RuntimeException("User not found"));

        // Call the method under test and expect an exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authenticationService.createAuthenticationToken(authenticationRequestDTO));

        // Verify the exception message
        assertEquals("User not found", exception.getMessage());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(any(String.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }
}