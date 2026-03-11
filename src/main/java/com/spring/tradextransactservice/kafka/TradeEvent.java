package com.spring.tradextransactservice.kafka;

import com.spring.tradextransactservice.Enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeEvent {
    private String idempotencyKey;
    private Long tradeId;
    private Long userId;
    private Long stockId;
    private Integer quantity;
    private TradeType tradeType;
    private BigDecimal executionPrice;
}
