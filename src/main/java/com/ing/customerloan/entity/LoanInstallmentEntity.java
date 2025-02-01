package com.ing.customerloan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "LOAN_INSTALLMENT")
public class
LoanInstallmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "LOAN_ID")
    private Long loanId;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "PAID_AMOUNT")
    private BigDecimal paidAmount;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "PAYMENT_DATE")
    private LocalDate paymentDate;

    @Column(name = "IS_PAID")
    private Boolean isPaid = false;
}