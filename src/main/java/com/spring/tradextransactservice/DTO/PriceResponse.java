package com.spring.tradextransactservice.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PriceResponse {
    private String symbol;
    private BigDecimal price;

    public PriceResponse(String symbol, BigDecimal price) {
        this.symbol = symbol;
        this.price = price;
    }
}
