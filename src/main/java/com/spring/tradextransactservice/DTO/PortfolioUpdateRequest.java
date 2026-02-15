package com.spring.tradextransactservice.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioUpdateRequest {
    private Long userId;
    private Long stockId;
    private Integer quantity;
    private BigDecimal price;

    public PortfolioUpdateRequest(Long userId, Long stockId, Integer quantity, BigDecimal price) {
        this.userId = userId;
        this.stockId = stockId;
        this.quantity = quantity;
        this.price = price;
    }
}
