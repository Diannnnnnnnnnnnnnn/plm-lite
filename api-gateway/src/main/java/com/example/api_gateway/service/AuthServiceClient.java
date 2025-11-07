package com.example.api_gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for communicating with Auth Service
 */
@Service
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(@Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    /**
     * Validate JWT token with Auth Service
     */
    public Mono<Boolean> validateToken(String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/auth/validate")
                        .queryParam("token", token)
                        .build())
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }
}

