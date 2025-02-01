package com.ing.customerloan.mapper;

import com.ing.customerloan.dto.LoanInstallmentResponseDTO;
import com.ing.customerloan.dto.LoanResponseDTO;
import com.ing.customerloan.entity.LoanEntity;
import com.ing.customerloan.entity.LoanInstallmentEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    List<LoanInstallmentResponseDTO> loanInstallmentEntitiesToLoanInstallmentResponseDTOList(List<LoanInstallmentEntity> loanInstallmentEntities);
    List<LoanResponseDTO> loanEntitiesToLoanResponseDTOList(List<LoanEntity> loanEntities);

}
