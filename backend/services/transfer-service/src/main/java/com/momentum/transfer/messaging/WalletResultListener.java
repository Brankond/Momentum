package com.momentum.transfer.messaging;

import com.momentum.transfer.domain.TransferSagaService;
import com.momentum.transfer.messaging.payload.WalletTransactionResultMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WalletResultListener {

    private final TransferSagaService transferSagaService;
    public WalletResultListener(TransferSagaService transferSagaService) {
        this.transferSagaService = transferSagaService;
    }

    @RabbitListener(queues = "${transfer.messaging.wallet-transaction-result-queue}")
    public void onWalletResult(WalletTransactionResultMessage message) {
        transferSagaService.handleWalletResult(message);
    }
}
