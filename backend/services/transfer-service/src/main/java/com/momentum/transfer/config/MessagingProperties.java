package com.momentum.transfer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "transfer.messaging")
public record MessagingProperties(
        String walletCommandExchange,
        String walletDebitRoutingKey,
        String walletCreditRoutingKey,
        String walletTransactionResultExchange,
        String walletTransactionResultQueue,
        String walletTransactionResultRoutingKey,
        String transferEventExchange,
        String transferCompletedRoutingKey,
        String transferFailedRoutingKey,
        String transferCompensationRoutingKey) {}
