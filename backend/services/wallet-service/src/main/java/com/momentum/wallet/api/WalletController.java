package com.momentum.wallet.api;

import com.momentum.sharedkernel.domain.value.Money;
import com.momentum.wallet.api.dto.CreateWalletRequest;
import com.momentum.wallet.api.dto.LedgerEntryResponse;
import com.momentum.wallet.api.dto.WalletResponse;
import com.momentum.wallet.api.dto.WalletTransactionRequest;
import com.momentum.wallet.domain.command.CreateWalletCommand;
import com.momentum.wallet.domain.command.WalletTransactionCommand;
import com.momentum.wallet.domain.model.LedgerEntrySnapshot;
import com.momentum.wallet.domain.model.WalletSnapshot;
import com.momentum.wallet.domain.service.WalletDomainService;
import jakarta.validation.Valid;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletDomainService walletDomainService;

    public WalletController(WalletDomainService walletDomainService) {
        this.walletDomainService = walletDomainService;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Currency currency = Currency.getInstance(request.currency());
        CreateWalletCommand command = new CreateWalletCommand(
                UUID.randomUUID(),
                request.userId(),
                request.externalUserId(),
                currency,
                request.initialBalanceMinorUnits() == null
                        ? null
                        : Money.ofMinor(request.initialBalanceMinorUnits(), currency));

        WalletSnapshot snapshot = walletDomainService.createWallet(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toWalletResponse(snapshot));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId) {
        WalletSnapshot snapshot = walletDomainService.getWallet(walletId);
        return ResponseEntity.ok(toWalletResponse(snapshot));
    }

    @GetMapping("/{walletId}/ledger")
    public ResponseEntity<List<LedgerEntryResponse>> getLedger(@PathVariable UUID walletId) {
        List<LedgerEntryResponse> entries = walletDomainService.getLedger(walletId).stream()
                .map(this::toLedgerEntryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(entries);
    }

    @PostMapping("/{walletId}/credit")
    public ResponseEntity<LedgerEntryResponse> credit(
            @PathVariable UUID walletId, @Valid @RequestBody WalletTransactionRequest request) {
        WalletTransactionCommand command = new WalletTransactionCommand(
                UUID.randomUUID(),
                walletId,
                request.amountMinorUnits(),
                request.reference(),
                request.description(),
                request.metadata(),
                null);

        LedgerEntrySnapshot snapshot = walletDomainService.credit(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toLedgerEntryResponse(snapshot));
    }

    @PostMapping("/{walletId}/debit")
    public ResponseEntity<LedgerEntryResponse> debit(
            @PathVariable UUID walletId, @Valid @RequestBody WalletTransactionRequest request) {
        WalletTransactionCommand command = new WalletTransactionCommand(
                UUID.randomUUID(),
                walletId,
                request.amountMinorUnits(),
                request.reference(),
                request.description(),
                request.metadata(),
                null);

        LedgerEntrySnapshot snapshot = walletDomainService.debit(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toLedgerEntryResponse(snapshot));
    }

    private WalletResponse toWalletResponse(WalletSnapshot snapshot) {
        return new WalletResponse(
                snapshot.walletId(),
                snapshot.userId(),
                snapshot.currency(),
                snapshot.balanceMinorUnits(),
                snapshot.status(),
                snapshot.updatedAt());
    }

    private LedgerEntryResponse toLedgerEntryResponse(LedgerEntrySnapshot snapshot) {
        return new LedgerEntryResponse(
                snapshot.entryId(),
                snapshot.walletId(),
                snapshot.type(),
                snapshot.amountMinorUnits(),
                snapshot.runningBalanceMinorUnits(),
                snapshot.reference(),
                snapshot.description(),
                snapshot.metadata(),
                snapshot.occurredAt());
    }
}
