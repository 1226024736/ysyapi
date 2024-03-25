package com.ysy.ysyapiclientsdk;

import com.ysy.ysyapiclientsdk.client.YsyApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ysyapi.client")
@Data
@ComponentScan
public class YsyApiClientConfig {
    private String accessKey;
    private String secretKey;
    @Bean
    public YsyApiClient YsyApiClient(){
        return new YsyApiClient(accessKey, secretKey);
    }
}
