package com.spring.tradextransactservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MarketPriceListener {

    private final Map<Long, BigDecimal> latestPricesCache = new ConcurrentHashMap<>();

    @KafkaListener(topics = "market-price-topic", groupId = "transact-service-group")
    public void consumeMarketPrice(MarketPriceEvent event) {
        log.debug("Received new market price: {} for stockId: {}", event.getPrice(), event.getStockId());
        latestPricesCache.put(event.getStockId(), event.getPrice());
    }

    public BigDecimal getLatestPrice(Long stockId) {
        BigDecimal price = latestPricesCache.get(stockId);
        if (price == null) {
            log.warn("Price for stockId {} not found in local cache. Returning null.", stockId);
            return null; // Handle fallback logic in Service
        }
        return price;
    }
}
