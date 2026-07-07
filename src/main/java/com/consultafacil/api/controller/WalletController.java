package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.wallet.WalletDTO;
import com.consultafacil.api.dto.billing.wallet.WalletTransactionDTO;
import com.consultafacil.application.port.in.wallet.GetAllWalletsUseCase;
import com.consultafacil.application.port.in.wallet.GetWalletTransactionsUseCase;
import com.consultafacil.application.port.in.wallet.GetWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WalletController {

    private final GetWalletUseCase getWalletUseCase;
    private final GetWalletTransactionsUseCase getWalletTransactionsUseCase;
    private final GetAllWalletsUseCase getAllWalletsUseCase;

    @GetMapping("/wallet/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WalletDTO> myWallet(Authentication authentication) {
        return ResponseEntity.ok(getWalletUseCase.execute(authentication.getName()));
    }

    @GetMapping("/wallet/me/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WalletTransactionDTO>> myTransactions(Authentication authentication) {
        return ResponseEntity.ok(getWalletTransactionsUseCase.execute(authentication.getName()));
    }

    @GetMapping("/admin/billing/wallets")
    @PreAuthorize("@adminPolicy.canManageWallets(authentication)")
    public ResponseEntity<List<WalletDTO>> adminListAll() {
        return ResponseEntity.ok(getAllWalletsUseCase.execute());
    }

    @GetMapping("/admin/billing/wallets/{userId}")
    @PreAuthorize("@adminPolicy.canManageWallets(authentication)")
    public ResponseEntity<WalletDTO> adminGetWallet(@PathVariable String userId) {
        return ResponseEntity.ok(getWalletUseCase.execute(userId));
    }

    @GetMapping("/admin/billing/wallets/{userId}/transactions")
    @PreAuthorize("@adminPolicy.canManageWallets(authentication)")
    public ResponseEntity<List<WalletTransactionDTO>> adminGetTransactions(@PathVariable String userId) {
        return ResponseEntity.ok(getWalletTransactionsUseCase.execute(userId));
    }
}
