package com.spring.tradextransactservice.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TradeRequest {
    private String stockSymbol;
    private Integer quantity;

    public TradeRequest(String stockSymbol, Integer quantity) {
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
    }
}