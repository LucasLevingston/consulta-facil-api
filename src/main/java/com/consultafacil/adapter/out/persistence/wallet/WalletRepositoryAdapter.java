package com.consultafacil.adapter.out.persistence.wallet;

import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import com.consultafacil.domain.repository.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepositoryPort {

    private final WalletRepository walletRepository;

    @Override
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public Optional<Wallet> findByUserId(String userId) {
        return walletRepository.findByUserId(userId);
    }

    @Override
    public List<Wallet> findAll() {
        return walletRepository.findAll();
    }
}
