package com.bank.microservicePayment.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    // Define el WebClient como un bean para el CustomerService
    @Bean
    public WebClient customerWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080/customers")
                .build();
    }


    @Bean
    public WebClient accountWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081/accounts") // URL base del servicio de cuentas
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
