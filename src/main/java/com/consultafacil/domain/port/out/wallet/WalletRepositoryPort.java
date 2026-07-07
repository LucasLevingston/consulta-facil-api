package com.consultafacil.domain.port.out.wallet;

import com.consultafacil.domain.entity.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletRepositoryPort {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByUserId(String userId);
    List<Wallet> findAll();
}
