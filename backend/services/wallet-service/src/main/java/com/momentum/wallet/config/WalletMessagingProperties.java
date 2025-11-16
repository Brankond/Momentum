package com.momentum.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wallet.messaging")
public record WalletMessagingProperties(
        String commandQueue,
        String commandExchange,
        String debitRoutingKey,
        String creditRoutingKey,
        String eventExchange,
        String transactionResultRoutingKey) {}
