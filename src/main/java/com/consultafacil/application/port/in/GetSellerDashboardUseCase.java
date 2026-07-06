package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.seller.SellerDashboardDTO;

public interface GetSellerDashboardUseCase {
    SellerDashboardDTO execute(String userId);
}
