package com.spring.tradextransactservice.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TradeRequest {
    private Long stockId;
    private Integer quantity;

    public TradeRequest(Long stockId, Integer quantity) {
        this.stockId = stockId;
        this.quantity = quantity;
    }
}