package com.spring.tradextransactservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeResultEvent {
    private String idempotencyKey;
    private Long tradeId;
    private boolean success;
    private String errorMessage;
}
