package com.momentum.wallet.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.sharedkernel.domain.value.Money;
import com.momentum.sharedkernel.error.DomainException;
import com.momentum.wallet.domain.command.CreateWalletCommand;
import com.momentum.wallet.domain.command.WalletTransactionCommand;
import com.momentum.wallet.domain.model.LedgerEntrySnapshot;
import com.momentum.wallet.domain.model.WalletSnapshot;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class WalletDomainServiceTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private WalletDomainService walletDomainService;

    @Test
    @Transactional
    void createWallet_persistsInitialBalance() {
        UUID userId = UUID.randomUUID();
        CreateWalletCommand command = new CreateWalletCommand(
                UUID.randomUUID(), userId, "user@example.com", USD, Money.ofMinor(1_000L, USD));

        WalletSnapshot snapshot = walletDomainService.createWallet(command);

        assertThat(snapshot.walletId()).isNotNull();
        assertThat(snapshot.userId()).isEqualTo(userId);
        assertThat(snapshot.balanceMinorUnits()).isEqualTo(1_000L);
        assertThat(snapshot.currency()).isEqualTo("USD");
    }

    @Test
    @Transactional
    void credit_increasesBalanceAndCreatesLedgerEntry() {
        WalletSnapshot wallet = createWalletWithBalance(500L);
        String reference = "credit-123";
        WalletTransactionCommand tx = new WalletTransactionCommand(
                UUID.randomUUID(), wallet.walletId(), 250L, reference, "top up", null, null);

        LedgerEntrySnapshot entry = walletDomainService.credit(tx);
        WalletSnapshot updated = walletDomainService.getWallet(wallet.walletId());

        assertThat(entry.amountMinorUnits()).isEqualTo(250L);
        assertThat(entry.reference()).isEqualTo(reference);
        assertThat(entry.runningBalanceMinorUnits()).isEqualTo(750L);
        assertThat(updated.balanceMinorUnits()).isEqualTo(750L);
    }

    @Test
    @Transactional
    void credit_isIdempotentOnReference() {
        WalletSnapshot wallet = createWalletWithBalance(0L);
        String reference = "dedupe-1";
        WalletTransactionCommand tx = new WalletTransactionCommand(
                UUID.randomUUID(), wallet.walletId(), 100L, reference, null, null, null);

        LedgerEntrySnapshot first = walletDomainService.credit(tx);
        LedgerEntrySnapshot second = walletDomainService.credit(tx);
        WalletSnapshot updated = walletDomainService.getWallet(wallet.walletId());

        assertThat(second.entryId()).isEqualTo(first.entryId());
        assertThat(updated.balanceMinorUnits()).isEqualTo(100L);
    }

    @Test
    @Transactional
    void debit_failsWhenBalanceInsufficient() {
        WalletSnapshot wallet = createWalletWithBalance(100L);
        WalletTransactionCommand debit = new WalletTransactionCommand(
                UUID.randomUUID(), wallet.walletId(), 200L, "debit-1", "buy", null, null);

        assertThatThrownBy(() -> walletDomainService.debit(debit))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    @Transactional
    void createWallet_rejectsDuplicateCurrencyPerUser() {
        UUID userId = UUID.randomUUID();
        CreateWalletCommand first = new CreateWalletCommand(
                UUID.randomUUID(), userId, "duplicate@example.com", USD, Money.zero(USD));
        walletDomainService.createWallet(first);

        CreateWalletCommand duplicate = new CreateWalletCommand(
                UUID.randomUUID(), userId, "duplicate@example.com", USD, Money.zero(USD));

        assertThatThrownBy(() -> walletDomainService.createWallet(duplicate))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Wallet already exists");
    }

    private WalletSnapshot createWalletWithBalance(long balanceMinorUnits) {
        UUID userId = UUID.randomUUID();
        CreateWalletCommand command = new CreateWalletCommand(
                UUID.randomUUID(),
                userId,
                "user-%s@example.com".formatted(userId),
                USD,
                Money.ofMinor(balanceMinorUnits, USD));
        return walletDomainService.createWallet(command);
    }
}
