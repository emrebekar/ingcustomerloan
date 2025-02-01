package com.ing.customerloan.service;

import com.ing.customerloan.component.JwtUtil;
import com.ing.customerloan.dto.AuthenticationRequestDTO;
import com.ing.customerloan.dto.AuthenticationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthenticationResponseDTO createAuthenticationToken(AuthenticationRequestDTO authenticationRequestDTO){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequestDTO.getUsername(), authenticationRequestDTO.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequestDTO.getUsername());
        return AuthenticationResponseDTO.
                builder().
                token(jwtUtil.generateToken(userDetails)).
                build();
    }
}
