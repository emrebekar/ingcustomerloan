package com.ing.customerloan.controller;

import com.ing.customerloan.dto.*;
import com.ing.customerloan.service.LoanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loans")
@SecurityRequirement(name = "bearerAuth")
@SuppressWarnings("unused")
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/create")
    public ResponseEntity<Void> createLoan(@RequestBody LoanRequestDTO request) {
        loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanResponseDTO>> getCustomerLoan(@PathVariable Long customerId) {
        return ResponseEntity.ok().body(loanService.getCustomerLoans(customerId));
    }

    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<LoanInstallmentResponseDTO>> getLoanInstallments(@PathVariable Long loanId) {
        return ResponseEntity.ok().body(loanService.getLoanInstallments(loanId));
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentResponseDTO> payLoan(@RequestBody PaymentRequestDTO paymentRequestDTO) {
        return ResponseEntity.ok().body(loanService.payLoan(paymentRequestDTO));
    }
}