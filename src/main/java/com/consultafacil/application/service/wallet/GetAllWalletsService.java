package com.consultafacil.application.service.wallet;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;
import com.consultafacil.application.port.in.wallet.GetAllWalletsUseCase;
import com.consultafacil.domain.port.out.wallet.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllWalletsService implements GetAllWalletsUseCase {

    private final WalletRepositoryPort walletRepository;
    private final WalletMapper walletMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WalletDTO> execute() {
        return walletRepository.findAll().stream()
                .map(walletMapper::toDTO)
                .collect(Collectors.toList());
    }
}
