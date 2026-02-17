package com.spring.tradextransactservice.DTO;

import com.spring.tradextransactservice.Models.Trade;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@NoArgsConstructor
public final class TradeMapper {

    public static TradeResponse toResponse(
            Trade trade,
            BigDecimal remainingWalletBalance

    ) {
        BigDecimal totalValue = trade.getPriceExecuted()
                .multiply(BigDecimal.valueOf(trade.getQuantity()));

        return new TradeResponse(
                trade.getId(),
                trade.getTradeType(),
                trade.getStockId(),
                trade.getQuantity(),
                trade.getPriceExecuted(),
                totalValue,
                remainingWalletBalance,
                trade.getExecutedAt()
        );
    }
}