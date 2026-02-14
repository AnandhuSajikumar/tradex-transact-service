package com.spring.tradextransactservice.Models;

import com.spring.tradextransactservice.Enums.TradeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "trades",
        indexes = {
                @Index(name = "idx_trade_user_time", columnList = "user_id, executedAt"),
                @Index(name = "idx_trade_stock_time", columnList = "stock_id, executedAt")
        }
)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceExecuted;

    @Column(nullable = false)
    private Instant executedAt;

    private Trade(Long userId, Long stockId,
                  TradeType tradeType,
                  int quantity, BigDecimal priceExecuted) {

        this.userId = userId;
        this.stockId = stockId;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.priceExecuted = priceExecuted;
        this.executedAt = Instant.now();
    }

    public static Trade create(
            Long userId,
            Long stockId,
            TradeType tradeType,
            int quantity,
            BigDecimal executionPrice
    ){
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if(executionPrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Execution price should be zero");
        }
        return new Trade(userId, stockId, tradeType, quantity, executionPrice);
    }

}