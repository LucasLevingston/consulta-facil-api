package com.consultafacil.domain.entity;

import com.consultafacil.core.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    private Wallet wallet(String balance, String pending) {
        return Wallet.builder()
                .userId("user-1")
                .balance(new BigDecimal(balance))
                .pendingBalance(new BigDecimal(pending))
                .build();
    }

    @Test
    void newWallet_startsWithZeroBalances() {
        Wallet wallet = Wallet.builder().userId("user-1").build();

        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void credit_addsToAvailableBalance() {
        Wallet wallet = wallet("10.00", "0");

        wallet.credit(new BigDecimal("5.50"));

        assertThat(wallet.getBalance()).isEqualByComparingTo("15.50");
    }

    @Test
    void debit_subtractsFromAvailableBalance() {
        Wallet wallet = wallet("10.00", "0");

        wallet.debit(new BigDecimal("10.00"));

        assertThat(wallet.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    void debit_rejectsAmountsAboveTheBalance() {
        Wallet wallet = wallet("10.00", "50.00");

        assertThatThrownBy(() -> wallet.debit(new BigDecimal("10.01")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Saldo insuficiente");
        assertThat(wallet.getBalance()).isEqualByComparingTo("10.00");
    }

    @Test
    void addPending_accumulatesPendingBalanceOnly() {
        Wallet wallet = wallet("1.00", "2.00");

        wallet.addPending(new BigDecimal("3.00"));

        assertThat(wallet.getPendingBalance()).isEqualByComparingTo("5.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("1.00");
    }

    @Test
    void releasePending_subtractsFromPendingBalance() {
        Wallet wallet = wallet("0", "8.00");

        wallet.releasePending(new BigDecimal("3.00"));

        assertThat(wallet.getPendingBalance()).isEqualByComparingTo("5.00");
    }

    @Test
    void releasePending_neverGoesBelowZero() {
        Wallet wallet = wallet("0", "2.00");

        wallet.releasePending(new BigDecimal("5.00"));

        assertThat(wallet.getPendingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
