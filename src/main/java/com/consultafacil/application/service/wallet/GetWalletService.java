package com.consultafacil.application.service.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;
import com.consultafacil.application.port.in.wallet.GetWalletUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWalletService implements GetWalletUseCase {

    private final WalletRepositoryPort walletRepository;
    private final WalletMapper walletMapper;

    @Override
    @Transactional(readOnly = true)
    public WalletDTO execute(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId));
        return walletMapper.toDTO(wallet);
    }
}
