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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class LoanService {

    private final ApplicationMapper applicationMapper;
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

    private final HttpServletRequest httpServletRequest;
    private static final Integer INSTALLMENT_LIMIT = 3;
    private static final Double PENALTY_MULTIPLIER = 0.001;
    public void createLoan(LoanRequestDTO loanRequestDTO) {
        if(Boolean.FALSE.equals(isCurrentUserAdmin()) && Boolean.FALSE.equals(isCurrentUserCustomerId(loanRequestDTO.getCustomerId()))){
            throw new AccessDeniedException("Only admin user could create loan for different users.");
        }
        Long customerId = loanRequestDTO.getCustomerId();
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Customer id couldn't be founded for creating loan."));

        checkCompliance(
                loanRequestDTO.getAmount(),
                loanRequestDTO.getInterestRate(),
                loanRequestDTO.getNumberOfInstallments(),
                customerEntity.getCreditLimit(),
                customerEntity.getUsedCreditLimit());

        calculateAndCreateLoan(
                customerEntity.getId(),
                loanRequestDTO.getAmount(),
                loanRequestDTO.getInterestRate(),
                loanRequestDTO.getNumberOfInstallments());

        customerEntity.setUsedCreditLimit(loanRequestDTO.getAmount().add(customerEntity.getUsedCreditLimit()));
        customerRepository.save(customerEntity);
    }

    private void calculateAndCreateLoan(Long customerId, BigDecimal requestedAmount, Float interestRate, Integer numberOfInstallments) {
        BigDecimal baseInstallmentAmount = requestedAmount.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
        BigDecimal installmentAmount = baseInstallmentAmount.multiply(BigDecimal.valueOf(1 + interestRate));

        LoanEntity loanEntity = new LoanEntity();
        loanEntity.setLoanAmount(requestedAmount);
        loanEntity.setCustomerId(customerId);
        loanEntity.setNumberOfInstallment(numberOfInstallments);

        loanRepository.save(loanEntity);
        createLoanInstallments(loanEntity.getId(), installmentAmount, numberOfInstallments);
    }

    private void createLoanInstallments(Long loanId, BigDecimal installmentAmount, Integer numberOfInstallments) {
        LocalDate firstDayOfNextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        List<LoanInstallmentEntity> loanInstallmentEntities = new ArrayList<>();
        for (int i = 0; i < numberOfInstallments; i++) {
            LoanInstallmentEntity loanInstallmentEntity = new LoanInstallmentEntity();
            loanInstallmentEntity.setLoanId(loanId);
            loanInstallmentEntity.setAmount(installmentAmount);
            loanInstallmentEntity.setDueDate(firstDayOfNextMonth.plusMonths(i).withDayOfMonth(1));
            loanInstallmentEntities.add(loanInstallmentEntity);
        }

        loanInstallmentRepository.saveAll(loanInstallmentEntities);
    }

    private void checkCompliance(BigDecimal requestedAmount, Float interestRate, Integer numberOfInstallments, BigDecimal customerCreditLimit, BigDecimal customerUserCreditLimit) {
        BigDecimal remainingCreditLimit = customerCreditLimit.subtract(customerUserCreditLimit);
        if (remainingCreditLimit.compareTo(requestedAmount) < 0) {
            throw new IllegalArgumentException("Amount couldn't be more than remaining credit limit.");
        }

        if (interestRate < 0.1 || interestRate > 0.5) {
            throw new IllegalArgumentException("Interest rate must be between 0.1 and 0.5");
        }

        if (!Set.of(6, 9, 12, 24).contains(numberOfInstallments)) {
            throw new IllegalArgumentException("Number of installments must be 6, 9, 12, or 24");
        }
    }

    public List<LoanResponseDTO> getCustomerLoans(Long customerId){
        if(Boolean.FALSE.equals(isCurrentUserAdmin()) && Boolean.FALSE.equals(isCurrentUserCustomerId(customerId))){
            throw new AccessDeniedException("Only admin users can show the customer loans of different users.");
        }

        List<LoanEntity> loanEntities = loanRepository.findByCustomerId(customerId);
        return applicationMapper.loanEntitiesToLoanResponseDTOList(loanEntities);
    }

    public List<LoanInstallmentResponseDTO> getLoanInstallments(Long loanId){
        LoanEntity loanEntity = loanRepository.findById(loanId).orElseThrow(()-> new IllegalArgumentException("Loan id couldn't be founded for getting loan installments."));
        if(Boolean.FALSE.equals(isCurrentUserAdmin()) && Boolean.FALSE.equals(isCurrentUserCustomerId(loanEntity.getCustomerId()))){
            throw new AccessDeniedException("Only admin users can show the customer loan installments of different users.");
        }
        List<LoanInstallmentEntity> loanInstallmentEntities = loanInstallmentRepository.findByLoanId(loanId, Sort.by(Sort.Direction.ASC, "dueDate"));
        return applicationMapper.loanInstallmentEntitiesToLoanInstallmentResponseDTOList(loanInstallmentEntities);
    }

    public PaymentResponseDTO payLoan(PaymentRequestDTO paymentRequestDTO){
        LoanEntity loanEntity = loanRepository.findById(paymentRequestDTO.getLoanId()).orElseThrow(() -> new IllegalArgumentException("Loan id couldn't be founded for making payment."));
        if(Boolean.FALSE.equals(isCurrentUserAdmin()) && Boolean.FALSE.equals(isCurrentUserCustomerId(loanEntity.getCustomerId()))){
            throw new AccessDeniedException("Only admin users can make payments the for different users.");
        }

        List<LoanInstallmentEntity> unpaidLoanInstallmentEntities =
                loanInstallmentRepository.findByLoanId(loanEntity.getId(), Sort.by(Sort.Direction.ASC, "dueDate")).
                        stream().filter(e -> e.getIsPaid().equals(Boolean.FALSE)).toList();
        PaymentResponseDTO paymentResponseDTO = payInstallments(unpaidLoanInstallmentEntities, paymentRequestDTO.getPaymentAmount());

        List<LoanInstallmentEntity> remainingUnpaidLoanInstallmentEntities = unpaidLoanInstallmentEntities.stream().filter(e -> e.getIsPaid().equals(Boolean.FALSE)).toList();
        if(remainingUnpaidLoanInstallmentEntities.isEmpty()){
            loanEntity.setIsPaid(true);
            loanRepository.save(loanEntity);

            CustomerEntity customerEntity = customerRepository.findById(loanEntity.getCustomerId()).orElseThrow(() -> new IllegalArgumentException("Customer id couldn't be founded for making payment."));
            customerEntity.setUsedCreditLimit(customerEntity.getUsedCreditLimit().subtract(loanEntity.getLoanAmount()));
            customerRepository.save(customerEntity);
        }

        return paymentResponseDTO;
    }

    private PaymentResponseDTO payInstallments(List<LoanInstallmentEntity> unpaidLoanInstalmentEntities, BigDecimal paymentAmount){
        BigDecimal totalPayment = new BigDecimal(0);
        int numberOfPaidInstallment = 0;
        LocalDate paymentDate = LocalDate.now();
        for(int i = 0; i < unpaidLoanInstalmentEntities.size(); i++){
            LoanInstallmentEntity unpaidLoanInstalmentEntity = unpaidLoanInstalmentEntities.get(i);
            BigDecimal addedPayment = totalPayment.add(unpaidLoanInstalmentEntity.getAmount());
            if(addedPayment.compareTo(paymentAmount) <= 0 || INSTALLMENT_LIMIT > i){
                totalPayment = addedPayment;
                numberOfPaidInstallment = i + 1;

                unpaidLoanInstalmentEntity.setIsPaid(true);
                unpaidLoanInstalmentEntity.setPaidAmount(calculatePenaltyAmount(unpaidLoanInstalmentEntity.getAmount(), unpaidLoanInstalmentEntity.getDueDate(), paymentDate));
                unpaidLoanInstalmentEntity.setPaymentDate(paymentDate);

                loanInstallmentRepository.save(unpaidLoanInstalmentEntity);
            }
            else{
                break;
            }
        }
        return preparePaymentResponse(totalPayment, numberOfPaidInstallment);
    }

    private BigDecimal calculatePenaltyAmount(BigDecimal installmentAmount, LocalDate dueDate, LocalDate paymentDate){
        long daysBetween = ChronoUnit.DAYS.between(dueDate, paymentDate);
        return installmentAmount.add(installmentAmount.multiply(BigDecimal.valueOf(PENALTY_MULTIPLIER * daysBetween)));
    }

    private PaymentResponseDTO preparePaymentResponse(BigDecimal paidAmount, Integer numberOfPaidInstallment){
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setTotalPaidAmount(paidAmount);
        paymentResponseDTO.setPaidInstallmentCount(numberOfPaidInstallment);

        return paymentResponseDTO;
    }

    private Boolean isCurrentUserAdmin(){
        String role = (String)httpServletRequest.getAttribute("userRole");
        return "ROLE_ADMIN".equals(role);
    }

    private Boolean isCurrentUserCustomerId(Long customerId){
        return customerId.equals(httpServletRequest.getAttribute("customerId"));
    }
}
