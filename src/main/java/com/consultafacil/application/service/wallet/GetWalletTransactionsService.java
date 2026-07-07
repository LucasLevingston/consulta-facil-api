package com.consultafacil.application.service.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletTransactionDTO;
import com.consultafacil.application.port.in.GetOrCreateWalletUseCase;
import com.consultafacil.application.port.in.GetWalletTransactionsUseCase;
import com.consultafacil.domain.entity.Wallet;
import com.consultafacil.domain.port.out.WalletTransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetWalletTransactionsService implements GetWalletTransactionsUseCase {

    private final GetOrCreateWalletUseCase getOrCreateWalletUseCase;
    private final WalletTransactionRepositoryPort walletTransactionRepository;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionDTO> execute(String userId) {
        Wallet wallet = getOrCreateWalletUseCase.execute(userId);
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()).stream()
                .map(walletTransactionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
