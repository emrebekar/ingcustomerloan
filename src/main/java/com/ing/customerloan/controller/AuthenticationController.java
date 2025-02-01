package com.ing.customerloan.controller;

import com.ing.customerloan.dto.AuthenticationRequestDTO;
import com.ing.customerloan.dto.AuthenticationResponseDTO;
import com.ing.customerloan.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@SuppressWarnings("unused")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> createAuthenticationToken(@RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        return ResponseEntity.ok(authenticationService.createAuthenticationToken(authenticationRequestDTO));
    }
}
