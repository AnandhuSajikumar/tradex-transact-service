package com.spring.tradextransactservice.Clients;

import com.spring.tradextransactservice.DTO.PriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class MarketClient {

    private final WebClient webClient;

    public BigDecimal getPrice(Long stockId) {
        return webClient.get()
                .uri("http://localhost:8084/v1/api/market/{symbol}", stockId)
                .retrieve()
                .bodyToMono(PriceResponse.class)
                .block()
                .getPrice();
    }
}