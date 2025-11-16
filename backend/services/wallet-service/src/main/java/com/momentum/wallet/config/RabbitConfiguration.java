package com.momentum.wallet.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@EnableConfigurationProperties(WalletMessagingProperties.class)
public class RabbitConfiguration {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public TopicExchange walletCommandExchange(WalletMessagingProperties properties) {
        return new TopicExchange(properties.commandExchange(), true, false);
    }

    @Bean
    public TopicExchange walletEventExchange(WalletMessagingProperties properties) {
        return new TopicExchange(properties.eventExchange(), true, false);
    }

    @Bean
    public Queue walletCommandQueue(WalletMessagingProperties properties) {
        return new Queue(properties.commandQueue(), true);
    }

    @Bean
    public Binding debitCommandBinding(
            Queue walletCommandQueue, TopicExchange walletCommandExchange, WalletMessagingProperties properties) {
        return BindingBuilder.bind(walletCommandQueue)
                .to(walletCommandExchange)
                .with(properties.debitRoutingKey());
    }

    @Bean
    public Binding creditCommandBinding(
            Queue walletCommandQueue, TopicExchange walletCommandExchange, WalletMessagingProperties properties) {
        return BindingBuilder.bind(walletCommandQueue)
                .to(walletCommandExchange)
                .with(properties.creditRoutingKey());
    }
}
