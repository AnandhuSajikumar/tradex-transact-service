package com.spring.tradextransactservice.DTO;

import com.spring.tradextransactservice.Enums.TradeType;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class TradeResponse {

    private final Long tradeId;
    private final TradeType type;
    private final String symbol;
    private final int quantity;
    private final BigDecimal priceExecuted;
    private final BigDecimal totalValue;
    private final BigDecimal remainingWalletBalance;
    private final Instant timestamp;

    public TradeResponse(
            Long tradeId,
            TradeType type,
            String symbol,
            int quantity,
            BigDecimal priceExecuted,
            BigDecimal totalValue,
            BigDecimal remainingWalletBalance,
            Instant timestamp
    ) {
        this.tradeId = tradeId;
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.priceExecuted = priceExecuted;
        this.totalValue = totalValue;
        this.remainingWalletBalance = remainingWalletBalance;
        this.timestamp = timestamp;
    }
}
