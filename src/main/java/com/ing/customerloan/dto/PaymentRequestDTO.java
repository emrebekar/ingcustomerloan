package com.ing.customerloan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    private Long loanId;
    private BigDecimal paymentAmount;
}
