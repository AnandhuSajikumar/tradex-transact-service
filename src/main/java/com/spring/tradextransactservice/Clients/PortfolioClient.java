package com.spring.tradextransactservice.Clients;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PortfolioClient {

    private final RestTemplate restTemplate;

    public void updateBuy(Long userId, Long stockId, Integer qty, BigDecimal price){
        PortfolioUpdateRequest request = new PortfolioUpdateRequest(userId, stockId, qty, price);

        restTemplate.postForObject(
                "http://localhost:8082/internal/portfolio/buy",
                request,
                void.class
        );
    }

    public void updateSell(Long userId, Long stockID, Integer qty){
        var request = new PortfolioUpdateRequest(userId, stockID, qty, null);

        restTemplate.postForObject(
                "http://localhost:8082/internal/portfolio/buy",
                request,
                void.class
        );
    }
}
