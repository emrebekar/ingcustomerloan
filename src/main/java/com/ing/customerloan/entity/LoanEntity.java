package com.ing.customerloan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "LOAN")
public class LoanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "LOAN_AMOUNT")
    private BigDecimal loanAmount;

    @Column(name = "NUMBER_OF_INSTALLMENT")
    private Integer numberOfInstallment;

    @CreationTimestamp
    @Column(name = "CREATE_DATE", updatable = false, nullable = false)
    private LocalDate createDate;

    @Column(name = "IS_PAID")
    private Boolean isPaid = false;
}