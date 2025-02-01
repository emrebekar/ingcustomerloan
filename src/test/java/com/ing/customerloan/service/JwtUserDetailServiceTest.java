package com.ing.customerloan.service;

import com.ing.customerloan.entity.UserEntity;
import com.ing.customerloan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtUserDetailService jwtUserDetailService;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up mock user
        mockUser = new UserEntity();
        mockUser.setUsername("testUser");
        mockUser.setPassword("password123");
        mockUser.setRole("ROLE_USER");
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        // Arrange: Mock the repository to return the mock user
        when(userRepository.findByUsername("testUser")).thenReturn(java.util.Optional.of(mockUser));

        // Act: Call the service method
        UserDetails userDetails = jwtUserDetailService.loadUserByUsername("testUser");

        // Assert: Check that the returned user details match the expected values
        assertEquals("testUser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));

        // Verify that the repository was called once with the username
        verify(userRepository, times(1)).findByUsername("testUser");
    }

    @Test
    void loadUserByUsername_UserDoesNotExist_ThrowsUsernameNotFoundException() {
        // Arrange: Mock the repository to return an empty Optional
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(java.util.Optional.empty());

        // Act & Assert: Check that the UsernameNotFoundException is thrown
        assertThrows(UsernameNotFoundException.class, () -> jwtUserDetailService.loadUserByUsername("nonExistentUser"));

        // Verify that the repository was called once with the username
        verify(userRepository, times(1)).findByUsername("nonExistentUser");
    }
}
