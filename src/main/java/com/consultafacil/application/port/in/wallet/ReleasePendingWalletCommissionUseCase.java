package com.consultafacil.application.port.in.wallet;

import java.math.BigDecimal;

public interface ReleasePendingWalletCommissionUseCase {
    void execute(String userId, BigDecimal amount);
}
