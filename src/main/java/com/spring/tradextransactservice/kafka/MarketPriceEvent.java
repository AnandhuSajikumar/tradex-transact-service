package com.spring.tradextransactservice.kafka;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor
@ToString
public class MarketPriceEvent {
    private Long stockId;
    private String symbol;
    private BigDecimal price;
    private Instant timestamp;

    public MarketPriceEvent(Long stockId, String symbol, BigDecimal price, Instant timestamp) {
        this.stockId = stockId;
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }
}
