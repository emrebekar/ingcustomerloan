package com.ing.customerloan.service;

import com.ing.customerloan.dto.*;
import com.ing.customerloan.entity.CustomerEntity;
import com.ing.customerloan.entity.LoanEntity;
import com.ing.customerloan.entity.LoanInstallmentEntity;
import com.ing.customerloan.mapper.ApplicationMapper;
import com.ing.customerloan.repository.CustomerRepository;
import com.ing.customerloan.repository.LoanInstallmentRepository;
import com.ing.customerloan.repository.LoanRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private LoanService loanService;

    private CustomerEntity customerEntity;
    private LoanEntity loanEntity;
    private LoanInstallmentEntity loanInstallmentEntity;
    private LoanRequestDTO loanRequestDTO;
    private PaymentRequestDTO paymentRequestDTO;

    @BeforeEach
    void setUp() {
        customerEntity = new CustomerEntity();
        customerEntity.setId(1L);
        customerEntity.setCreditLimit(BigDecimal.valueOf(10000));
        customerEntity.setUsedCreditLimit(BigDecimal.valueOf(5000));

        loanEntity = new LoanEntity();
        loanEntity.setId(1L);
        loanEntity.setCustomerId(1L);
        loanEntity.setLoanAmount(BigDecimal.valueOf(5000));
        loanEntity.setNumberOfInstallment(12);

        loanInstallmentEntity = new LoanInstallmentEntity();
        loanInstallmentEntity.setId(1L);
        loanInstallmentEntity.setLoanId(1L);
        loanInstallmentEntity.setAmount(BigDecimal.valueOf(500));
        loanInstallmentEntity.setDueDate(LocalDate.now().plusMonths(1));
        loanInstallmentEntity.setIsPaid(false);

        loanRequestDTO = new LoanRequestDTO();
        loanRequestDTO.setCustomerId(1L);
        loanRequestDTO.setAmount(BigDecimal.valueOf(5000));
        loanRequestDTO.setInterestRate(0.2f);
        loanRequestDTO.setNumberOfInstallments(12);

        paymentRequestDTO = new PaymentRequestDTO();
        paymentRequestDTO.setLoanId(1L);
        paymentRequestDTO.setPaymentAmount(BigDecimal.valueOf(1000));
    }

    @Test
    void testCreateLoan_Success_AdminUser() {
        // Mock admin user
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_ADMIN");

        when(customerRepository.findById(any(Long.class))).thenReturn(Optional.of(customerEntity));
        when(loanRepository.save(any(LoanEntity.class))).thenReturn(loanEntity);
        when(loanInstallmentRepository.saveAll(any(List.class))).thenReturn(Collections.singletonList(loanInstallmentEntity));

        loanService.createLoan(loanRequestDTO);

        verify(customerRepository, times(1)).findById(any(Long.class));
        verify(loanRepository, times(1)).save(any(LoanEntity.class));
        verify(loanInstallmentRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    void testCreateLoan_Success_SameCustomer() {
        // Mock same customer
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_USER");
        when(httpServletRequest.getAttribute("customerId")).thenReturn(1L);

        when(customerRepository.findById(any(Long.class))).thenReturn(Optional.of(customerEntity));
        when(loanRepository.save(any(LoanEntity.class))).thenReturn(loanEntity);
        when(loanInstallmentRepository.saveAll(any(List.class))).thenReturn(Collections.singletonList(loanInstallmentEntity));

        loanService.createLoan(loanRequestDTO);

        verify(customerRepository, times(1)).findById(any(Long.class));
        verify(loanRepository, times(1)).save(any(LoanEntity.class));
        verify(loanInstallmentRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    void testCreateLoan_AccessDenied_DifferentCustomer() {
        // Mock different customer
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_USER");
        when(httpServletRequest.getAttribute("customerId")).thenReturn(2L);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> loanService.createLoan(loanRequestDTO));

        assertEquals("Only admin user could create loan for different users.", exception.getMessage());
    }

    @Test
    void testGetCustomerLoans_Success_AdminUser() {
        // Mock admin user
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_ADMIN");

        when(loanRepository.findByCustomerId(any(Long.class))).thenReturn(Collections.singletonList(loanEntity));
        when(applicationMapper.loanEntitiesToLoanResponseDTOList(any(List.class))).thenReturn(Collections.singletonList(new LoanResponseDTO()));

        List<LoanResponseDTO> result = loanService.getCustomerLoans(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findByCustomerId(any(Long.class));
        verify(applicationMapper, times(1)).loanEntitiesToLoanResponseDTOList(any(List.class));
    }

    @Test
    void testGetCustomerLoans_AccessDenied_DifferentCustomer() {
        // Mock different customer
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_USER");
        when(httpServletRequest.getAttribute("customerId")).thenReturn(2L);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> loanService.getCustomerLoans(1L));

        assertEquals("Only admin users can show the customer loans of different users.", exception.getMessage());
    }

    @Test
    void testGetLoanInstallments_Success_AdminUser() {
        // Mock admin user
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_ADMIN");

        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loanEntity));
        when(loanInstallmentRepository.findByLoanId(any(Long.class), any(Sort.class))).thenReturn(Collections.singletonList(loanInstallmentEntity));
        when(applicationMapper.loanInstallmentEntitiesToLoanInstallmentResponseDTOList(any(List.class))).thenReturn(Collections.singletonList(new LoanInstallmentResponseDTO()));

        List<LoanInstallmentResponseDTO> result = loanService.getLoanInstallments(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findById(any(Long.class));
        verify(loanInstallmentRepository, times(1)).findByLoanId(any(Long.class), any(Sort.class));
        verify(applicationMapper, times(1)).loanInstallmentEntitiesToLoanInstallmentResponseDTOList(any(List.class));
    }

    @Test
    void testGetLoanInstallments_AccessDenied_DifferentCustomer() {
        // Mock different customer
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_USER");
        when(httpServletRequest.getAttribute("customerId")).thenReturn(2L);

        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loanEntity));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> loanService.getLoanInstallments(1L));

        assertEquals("Only admin users can show the customer loan installments of different users.", exception.getMessage());
    }

    @Test
    void testPayLoan_Success_AdminUser() {
        // Mock admin user
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_ADMIN");

        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loanEntity));
        when(loanInstallmentRepository.findByLoanId(any(Long.class), any(Sort.class))).thenReturn(Collections.singletonList(loanInstallmentEntity));
        when(customerRepository.findById(any(Long.class))).thenReturn(Optional.of(customerEntity));

        PaymentResponseDTO result = loanService.payLoan(paymentRequestDTO);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500), result.getTotalPaidAmount());
        assertEquals(1, result.getPaidInstallmentCount());
        verify(loanRepository, times(1)).findById(any(Long.class));
        verify(loanInstallmentRepository, times(1)).findByLoanId(any(Long.class), any(Sort.class));
        verify(customerRepository, times(1)).findById(any(Long.class));
    }

    @Test
    void testPayLoan_AccessDenied_DifferentCustomer() {
        // Mock different customer
        when(httpServletRequest.getAttribute("userRole")).thenReturn("ROLE_USER");
        when(httpServletRequest.getAttribute("customerId")).thenReturn(2L);

        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loanEntity));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> loanService.payLoan(paymentRequestDTO));

        assertEquals("Only admin users can make payments the for different users.", exception.getMessage());
    }
}