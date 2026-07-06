package com.consultafacil.application.port.in;

import java.math.BigDecimal;

public interface AddPendingWalletCommissionUseCase {
    void execute(String userId, BigDecimal amount, String commissionId);
}
