package com.consultafacil.application.port.in.wallet;

import java.math.BigDecimal;

public interface DebitWalletUseCase {
    void execute(String userId, BigDecimal amount, String description);
}
