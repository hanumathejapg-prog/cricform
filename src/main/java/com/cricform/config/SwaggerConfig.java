package com.cricform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI cricFormOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CricForm API")
                .description("Cricket player form analytics API")
                .version("v1"));
    }
}
