package com.ysy.ysyapigateway.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
    private static final int TTL_TIME = 30 * 60 * 1000;
    /**
     * 创建ttl.queue，到时间将消息传递给死信队列
     * @return
     */
    @Bean
    public Queue ttlQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", TTL_TIME); // 消息TTL时间，单位为毫秒
        args.put("x-dead-letter-exchange", "dlx.exchange"); // 指定DLX
        args.put("x-dead-letter-routing-key", "dlx.routing.key"); // DLX的路由键
        return new Queue("ttl.queue", true, false, false, args);
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx.exchange");
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    Queue dlxQueue() {
        return new Queue("dlx.queue");
    }

    /**
     * 将死信队列绑定到死信交换机上，并且设置路由键dlx.routing.key
     * @return
     */
    @Bean
    Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(deadLetterExchange()).with("dlx.routing.key");
    }
}
