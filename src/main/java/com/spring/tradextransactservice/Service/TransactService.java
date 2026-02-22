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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.tradextransactservice.Enums.IdempotencyStatus;
import com.spring.tradextransactservice.Models.IdempotencyKey;
import com.spring.tradextransactservice.Service.IdempotencyService;

@Service
@RequiredArgsConstructor
public class TransactService {

        private final TradeRepository tradeRepository;
        private final AccountRepository accountRepository;
        private final MarketClient marketClient;
        private final PortfolioClient portfolioClient;
        private final IdempotencyService idempotencyService;
        private final ObjectMapper objectMapper;

        @Transactional
        public TradeResponse buyStock(String idempotencyKeyStr, Long userId, Long stockId, Integer quantity) {

                IdempotencyKey key = idempotencyService.createOrReturnKey(idempotencyKeyStr, userId);
                if (key != null && key.getStatus() == IdempotencyStatus.COMPLETED) {
                        try {
                                return objectMapper.readValue(key.getResponseBody(), TradeResponse.class);
                        } catch (JsonProcessingException e) {
                                throw new IllegalStateException("Failed to parse cached response");
                        }
                }

                try {
                        if (quantity <= 0)
                                throw new IllegalArgumentException("Quantity must be positive");

                        Account account = accountRepository.findByUserIdWithLock(userId)
                                        .orElseThrow(() -> new IllegalStateException("Account not found"));

                        BigDecimal executionPrice = marketClient.getPrice(stockId);

                        BigDecimal totalCost = executionPrice.multiply(BigDecimal.valueOf(quantity));

                        account.debitWallet(totalCost);

                        portfolioClient.updateBuy(idempotencyKeyStr, userId, stockId, quantity, executionPrice);

                        Trade trade = Trade.create(
                                        userId,
                                        stockId,
                                        TradeType.BUY,
                                        quantity,
                                        executionPrice);

                        tradeRepository.save(trade);
                        TradeResponse response = TradeMapper.toResponse(trade, account.getBalance());

                        idempotencyService.markCompleted(idempotencyKeyStr, response);
                        return response;

                } catch (Exception e) {
                        idempotencyService.markFailed(idempotencyKeyStr);
                        throw e;
                }
        }

        @Transactional
        public TradeResponse sellStock(String idempotencyKeyStr, Long userId, Long stockId, Integer quantity) {

                IdempotencyKey key = idempotencyService.createOrReturnKey(idempotencyKeyStr, userId);
                if (key != null && key.getStatus() == IdempotencyStatus.COMPLETED) {
                        try {
                                return objectMapper.readValue(key.getResponseBody(), TradeResponse.class);
                        } catch (JsonProcessingException e) {
                                throw new IllegalStateException("Failed to parse cached response");
                        }
                }

                try {
                        if (quantity <= 0)
                                throw new IllegalArgumentException("Quantity must be positive");

                        Account account = accountRepository.findByUserIdWithLock(userId)
                                        .orElseThrow(() -> new IllegalStateException("Account not found"));

                        BigDecimal executionPrice = marketClient.getPrice(stockId);

                        portfolioClient.updateSell(idempotencyKeyStr, userId, stockId, quantity);

                        BigDecimal totalValue = executionPrice.multiply(BigDecimal.valueOf(quantity));

                        account.creditWallet(totalValue);

                        Trade trade = Trade.create(
                                        userId,
                                        stockId,
                                        TradeType.SELL,
                                        quantity,
                                        executionPrice);

                        tradeRepository.save(trade);
                        TradeResponse response = TradeMapper.toResponse(trade, account.getBalance());

                        idempotencyService.markCompleted(idempotencyKeyStr, response);
                        return response;

                } catch (Exception e) {
                        idempotencyService.markFailed(idempotencyKeyStr);
                        throw e;
                }
        }

        public Page<TradeResponse> getAllTrade(Pageable pageable) {
                return tradeRepository.findAll(pageable)
                                .map(trade -> TradeMapper.toResponse(trade, null));
        }

        @Transactional
        public Page<TradeResponse> getTradeHistory(Long userId, Pageable pageable) {
                return tradeRepository.findByUserIdOrderByExecutedAtDesc(userId, pageable)
                                .map(trade -> TradeMapper.toResponse(trade, null));
        }
}
