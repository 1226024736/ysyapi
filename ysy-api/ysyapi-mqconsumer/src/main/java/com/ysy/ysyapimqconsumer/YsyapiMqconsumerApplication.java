package com.ysy.ysyapimqconsumer;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class YsyapiMqconsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YsyapiMqconsumerApplication.class, args);
    }
    @Bean
    public MessageConverter jacksonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
