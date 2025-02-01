package com.ing.customerloan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequestDTO {
    private Long customerId;
    private BigDecimal amount;
    private Float interestRate;
    private Integer numberOfInstallments;
}
