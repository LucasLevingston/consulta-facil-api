package com.consultafacil.application.service.wallet;

import com.consultafacil.application.port.in.wallet.GetOrCreateWalletUseCase;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOrCreateWalletService implements GetOrCreateWalletUseCase {

    private final WalletRepositoryPort walletRepository;

    @Override
    @Transactional
    public Wallet execute(String userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet wallet = Wallet.builder().userId(userId).build();
            return walletRepository.save(wallet);
        });
    }
}
