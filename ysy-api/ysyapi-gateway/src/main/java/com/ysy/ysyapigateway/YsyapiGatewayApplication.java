package com.ysy.ysyapigateway;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableDubbo
public class YsyapiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(YsyapiGatewayApplication.class, args);
    }

    @Bean
    public MessageConverter jacksonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
