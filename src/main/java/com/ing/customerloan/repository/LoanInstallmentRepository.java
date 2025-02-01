package com.ing.customerloan.repository;


import com.ing.customerloan.entity.LoanInstallmentEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallmentEntity, Long> {
    List<LoanInstallmentEntity> findByLoanId(Long loanId, Sort sort);
}
