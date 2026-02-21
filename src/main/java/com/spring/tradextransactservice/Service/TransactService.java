package com.spring.tradextransactservice.Service;

import com.spring.tradextransactservice.Clients.MarketClient;
import com.spring.tradextransactservice.Clients.PortfolioClient;
import com.spring.tradextransactservice.DTO.TradeMapper;
import com.spring.tradextransactservice.DTO.TradeResponse;
import com.spring.tradextransactservice.Enums.TradeType;
import com.spring.tradextransactservice.Models.Account;
import com.spring.tradextransactservice.Models.Trade;
import com.spring.tradextransactservice.Repository.AccountRepository;
import com.spring.tradextransactservice.Repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactService {

    private final TradeRepository tradeRepository;
    private final AccountRepository accountRepository;
    private final MarketClient marketClient;
    private final PortfolioClient portfolioClient;

    @Transactional
    public TradeResponse buyStock(Long userId, Long stockId, Integer quantity) {

        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        Account account = accountRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        BigDecimal executionPrice = marketClient.getPrice(stockId);

        BigDecimal totalCost = executionPrice.multiply(BigDecimal.valueOf(quantity));

        account.debitWallet(totalCost);

        portfolioClient.updateBuy(userId, stockId, quantity, executionPrice);

        Trade trade = Trade.create(
                userId,
                stockId,
                TradeType.BUY,
                quantity,
                executionPrice
        );

        tradeRepository.save(trade);

        return TradeMapper.toResponse(trade, account.getBalance());
    }

    @Transactional
    public TradeResponse sellStock(Long userId, Long stockId, Integer quantity) {

        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        Account account = accountRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        BigDecimal executionPrice = marketClient.getPrice(stockId);

        portfolioClient.updateSell(userId, stockId, quantity);

        BigDecimal totalValue = executionPrice.multiply(BigDecimal.valueOf(quantity));

        account.creditWallet(totalValue);

        Trade trade = Trade.create(
                userId,
                stockId,
                TradeType.SELL,
                quantity,
                executionPrice
        );

        tradeRepository.save(trade);

        return TradeMapper.toResponse(trade, account.getBalance());
    }

    public Page<TradeResponse> getAllTrade(Pageable pageable){
        return tradeRepository.findAll(pageable)
                .map(trade -> TradeMapper.toResponse(trade, null));
    }

    @Transactional
    public Page<TradeResponse> getTradeHistory (Long userId, Pageable pageable){
        return tradeRepository.findByUserIdOrderByExecutedAtDesc(userId, pageable)
                .map(trade -> TradeMapper.toResponse(trade, null));
    }
}

