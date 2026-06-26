package com.auragate.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.auragate.common.constant.Constants.*;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue aiTaskQueue() {
        return new Queue(AI_TASK_QUEUE, true);
    }

    @Bean
    public Queue logOperationQueue() {
        return new Queue(LOG_OPERATION_QUEUE, true);
    }

    @Bean
    public DirectExchange aiTaskExchange() {
        return new DirectExchange(AI_TASK_EXCHANGE);
    }

    @Bean
    public DirectExchange logExchange() {
        return new DirectExchange(LOG_EXCHANGE);
    }

    @Bean
    public Binding aiTaskBinding() {
        return BindingBuilder.bind(aiTaskQueue()).to(aiTaskExchange()).with(AI_TASK_ROUTING_KEY);
    }

    @Bean
    public Binding logOperationBinding() {
        return BindingBuilder.bind(logOperationQueue()).to(logExchange()).with(LOG_OPERATION_ROUTING_KEY);
    }
}
