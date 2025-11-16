package com.momentum.transfer.config;

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
@EnableConfigurationProperties(MessagingProperties.class)
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
    public TopicExchange walletCommandExchange(MessagingProperties properties) {
        return new TopicExchange(properties.walletCommandExchange(), true, false);
    }

    @Bean
    public TopicExchange walletTransactionResultExchange(MessagingProperties properties) {
        return new TopicExchange(properties.walletTransactionResultExchange(), true, false);
    }

    @Bean
    public TopicExchange transferEventExchange(MessagingProperties properties) {
        return new TopicExchange(properties.transferEventExchange(), true, false);
    }

    @Bean
    public Queue walletTransactionResultQueue(MessagingProperties properties) {
        return new Queue(properties.walletTransactionResultQueue(), true);
    }

    @Bean
    public Binding walletTransactionResultBinding(
            Queue walletTransactionResultQueue,
            TopicExchange walletTransactionResultExchange,
            MessagingProperties properties) {
        return BindingBuilder.bind(walletTransactionResultQueue)
                .to(walletTransactionResultExchange)
                .with(properties.walletTransactionResultRoutingKey());
    }
}
