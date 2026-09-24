package com.bsourichanh.javaspring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient userRestClient(@Value("${user-service.url:http://localhost:8081}") String userServiceUrl) {
        return RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }
}
