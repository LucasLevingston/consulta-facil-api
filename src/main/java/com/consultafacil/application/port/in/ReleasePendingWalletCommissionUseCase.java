package com.consultafacil.application.port.in;

import java.math.BigDecimal;

public interface ReleasePendingWalletCommissionUseCase {
    void execute(String userId, BigDecimal amount);
}
