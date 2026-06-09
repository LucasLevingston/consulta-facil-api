package com.consultafacil.api.dto.seller;

import com.consultafacil.domain.enums.SellerSaleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCommissionStatusDTO {

    @NotNull(message = "status é obrigatório")
    private SellerSaleStatus status;
}
