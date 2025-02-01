package com.ing.customerloan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentResponseDTO {
    private Integer paidInstallmentCount;
    private BigDecimal totalPaidAmount;
}
