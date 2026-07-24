package com.syncforge.syncforge.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SYNC_JOB_EXCHANGE = "syncforge.sync-job.exchange";
    public static final String SYNC_JOB_QUEUE = "syncforge.sync-job.queue";
    public static final String SYNC_JOB_ROUTING_KEY = "sync.job.created";

    @Bean
    public DirectExchange syncJobExchange() {
        return new DirectExchange(SYNC_JOB_EXCHANGE);
    }

    @Bean
    public Queue syncJobQueue() {
        return new Queue(SYNC_JOB_QUEUE, true);
    }

    @Bean
    public Binding syncJobBinding() {
        return BindingBuilder
                .bind(syncJobQueue())
                .to(syncJobExchange())
                .with(SYNC_JOB_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}