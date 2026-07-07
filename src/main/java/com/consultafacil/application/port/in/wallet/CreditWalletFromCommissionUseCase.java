package com.consultafacil.application.port.in.wallet;

import java.math.BigDecimal;

public interface CreditWalletFromCommissionUseCase {
    void execute(String userId, BigDecimal amount, String commissionId);
}
