package com.spring.tradextransactservice.Clients;

import com.spring.tradextransactservice.DTO.PriceResponse;
import com.spring.tradextransactservice.Exception.MarketUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketClient {

    private final WebClient webClient;

    @Retry(name = "marketClient", fallbackMethod = "marketFallback")
    @CircuitBreaker(name = "marketClient", fallbackMethod = "marketFallback")
    @TimeLimiter(name = "marketClient")
    public CompletableFuture<BigDecimal> getPriceAsync(Long stockId) {
        return CompletableFuture.supplyAsync(() -> webClient.get()
                .uri("http://localhost:8084/v1/api/market/{symbol}", stockId)
                .retrieve()
                .bodyToMono(PriceResponse.class)
                .map(PriceResponse::getPrice)
                .block());
    }

    public BigDecimal getPrice(Long stockId) {
        return getPriceAsync(stockId).join();
    }

    public CompletableFuture<BigDecimal> marketFallback(Long stockId, Throwable t) {
        log.error("Market Service is unavailable for stock {}. Reason: {}", stockId, t.getMessage());
        throw new MarketUnavailableException("Market Service is currently unavailable. Please try again later.");
    }
}