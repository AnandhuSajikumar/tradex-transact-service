package com.spring.tradextransactservice.Clients;

import com.spring.tradextransactservice.Exception.PortfolioUnavailableException;
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
public class PortfolioClient {

    private final WebClient webClient;

    @Retry(name = "portfolioClient", fallbackMethod = "portfolioFallback")
    @CircuitBreaker(name = "portfolioClient", fallbackMethod = "portfolioFallback")
    @TimeLimiter(name = "portfolioClient")
    public CompletableFuture<Void> updateBuyAsync(String idempotencyKey, Long userId, Long stockId, Integer quantity,
            BigDecimal price) {
        return CompletableFuture.supplyAsync(() -> {
            webClient.post()
                    .uri("http://localhost:8083/portfolio/buy")
                    .header("Idempotency-Key", idempotencyKey)
                    .bodyValue(new PortfolioUpdateRequest(userId, stockId, quantity, price))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return null;
        });
    }

    public void updateBuy(String idempotencyKey, Long userId, Long stockId, Integer quantity, BigDecimal price) {
        updateBuyAsync(idempotencyKey, userId, stockId, quantity, price).join();
    }

    @Retry(name = "portfolioClient", fallbackMethod = "portfolioFallback")
    @CircuitBreaker(name = "portfolioClient", fallbackMethod = "portfolioFallback")
    @TimeLimiter(name = "portfolioClient")
    public CompletableFuture<Void> updateSellAsync(String idempotencyKey, Long userId, Long stockId, Integer quantity) {
        return CompletableFuture.supplyAsync(() -> {
            webClient.post()
                    .uri("http://localhost:8083/portfolio/sell")
                    .header("Idempotency-Key", idempotencyKey)
                    .bodyValue(new PortfolioUpdateRequest(userId, stockId, quantity, null))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return null;
        });
    }

    public void updateSell(String idempotencyKey, Long userId, Long stockId, Integer quantity) {
        updateSellAsync(idempotencyKey, userId, stockId, quantity).join();
    }

    public CompletableFuture<Void> portfolioFallback(String idempotencyKey, Long userId, Long stockId, Integer quantity,
            BigDecimal price, Throwable t) {
        log.error("Portfolio Service unavailable for buy (User: {}, Stock: {}). Reason: {}", userId, stockId,
                t.getMessage());
        throw new PortfolioUnavailableException("Portfolio Service is currently unavailable. Please try again later.");
    }

    public CompletableFuture<Void> portfolioFallback(String idempotencyKey, Long userId, Long stockId, Integer quantity,
            Throwable t) {
        log.error("Portfolio Service unavailable for sell (User: {}, Stock: {}). Reason: {}", userId, stockId,
                t.getMessage());
        throw new PortfolioUnavailableException("Portfolio Service is currently unavailable. Please try again later.");
    }

    public void rollbackBuy(String idempotencyKey, Long userId, Long stockId, Integer quantity, BigDecimal price) {
        try {
            webClient.post()
                    .uri("http://localhost:8083/portfolio/rollback-buy")
                    .header("Idempotency-Key", idempotencyKey)
                    .bodyValue(new PortfolioUpdateRequest(userId, stockId, quantity, price))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Successfully rolled back buy operation for User: {}, Stock: {}", userId, stockId);
        } catch (Exception e) {
            log.error(
                    "Failed to rollback buy operation for User: {}, Stock: {}. Manual intervention may be required. Reason: {}",
                    userId, stockId, e.getMessage());
        }
    }

    public void rollbackSell(String idempotencyKey, Long userId, Long stockId, Integer quantity) {
        try {
            webClient.post()
                    .uri("http://localhost:8083/portfolio/rollback-sell")
                    .header("Idempotency-Key", idempotencyKey)
                    .bodyValue(new PortfolioUpdateRequest(userId, stockId, quantity, null))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Successfully rolled back sell operation for User: {}, Stock: {}", userId, stockId);
        } catch (Exception e) {
            log.error(
                    "Failed to rollback sell operation for User: {}, Stock: {}. Manual intervention may be required. Reason: {}",
                    userId, stockId, e.getMessage());
        }
    }
}