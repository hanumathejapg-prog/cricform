package com.cricform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate rapidApiRestTemplate(
            RestTemplateBuilder builder,
            @Value("${rapidapi.key}") String rapidApiKey,
            @Value("${rapidapi.host}") String rapidApiHost
    ) {
        return builder
                .defaultHeader("X-RapidAPI-Key", rapidApiKey)
                .defaultHeader("X-RapidAPI-Host", rapidApiHost)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }
}
