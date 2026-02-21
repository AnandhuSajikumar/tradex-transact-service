package com.spring.tradextransactservice.Clients;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PortfolioClient {

    private final WebClient webClient;

    public void updateBuy(Long userId, Long stockId, Integer quantity, BigDecimal price) {
        webClient.post()
                .uri("http://localhost:8083/portfolio/buy")
                .bodyValue(new PortfolioUpdateRequest(userId, stockId, quantity, price))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void updateSell(Long userId, Long stockId, Integer quantity) {
        webClient.post()
                .uri("http://localhost:8083/portfolio/sell")
                .bodyValue(new PortfolioUpdateRequest(userId, stockId, quantity, null))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}