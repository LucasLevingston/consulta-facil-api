package com.consultafacil.application.service.wallet;

import com.consultafacil.application.port.in.wallet.GetOrCreateWalletUseCase;
import com.consultafacil.application.port.in.wallet.ReleasePendingWalletCommissionUseCase;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReleasePendingWalletCommissionService implements ReleasePendingWalletCommissionUseCase {

    private final GetOrCreateWalletUseCase getOrCreateWalletUseCase;
    private final WalletRepositoryPort walletRepository;

    @Override
    @Transactional
    public void execute(String userId, BigDecimal amount) {
        Wallet wallet = getOrCreateWalletUseCase.execute(userId);
        wallet.releasePending(amount);
        walletRepository.save(wallet);
    }
}
