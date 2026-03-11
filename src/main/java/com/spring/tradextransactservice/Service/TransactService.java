package com.spring.tradextransactservice.Service;

import com.spring.tradextransactservice.kafka.MarketPriceListener;

import com.spring.tradextransactservice.DTO.TradeMapper;
import com.spring.tradextransactservice.DTO.TradeResponse;
import com.spring.tradextransactservice.Enums.TradeType;
import com.spring.tradextransactservice.Enums.TradeStatus;
import com.spring.tradextransactservice.kafka.TradeEvent;
import org.springframework.kafka.core.KafkaTemplate;
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

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactService {

        private final TradeRepository tradeRepository;
        private final AccountRepository accountRepository;

        private final MarketPriceListener marketPriceListener;
        private final KafkaTemplate<String, TradeEvent> kafkaTemplate;
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

                boolean portfolioUpdated = false;
                BigDecimal executionPrice = null;
                try {
                        if (quantity <= 0)
                                throw new IllegalArgumentException("Quantity must be positive");

                        Account account = accountRepository.findByUserIdWithLock(userId)
                                        .orElseThrow(() -> new IllegalStateException("Account not found"));

                        executionPrice = marketPriceListener.getLatestPrice(stockId);
                        if (executionPrice == null) {
                                throw new IllegalStateException(
                                                "Market price not available for stockId " + stockId + ". Please try again.");
                        }

                        BigDecimal totalCost = executionPrice.multiply(BigDecimal.valueOf(quantity));

                        account.debitWallet(totalCost);

                        Trade trade = Trade.create(
                                        userId,
                                        stockId,
                                        TradeType.BUY,
                                        quantity,
                                        executionPrice);
                        trade.setStatus(TradeStatus.PENDING);
                        tradeRepository.save(trade);

                        TradeEvent event = new TradeEvent(
                                        idempotencyKeyStr,
                                        trade.getId(),
                                        userId,
                                        stockId,
                                        quantity,
                                        TradeType.BUY,
                                        executionPrice);

                        kafkaTemplate.send("trade-requests-topic", idempotencyKeyStr, event);

                        TradeResponse response = TradeMapper.toResponse(trade, account.getBalance());

                        // Do not mark idempotency as completed yet! It's still pending.
                        // idempotencyService.markCompleted(idempotencyKeyStr, response);

                        return response;

                } catch (Exception e) {
                        idempotencyService.markFailed(idempotencyKeyStr);
                        // Local transaction rolls back wallet and trade DB save automatically.
                        // No need for explicit saga compensation here anymore because Portfolio wasn't
                        // called yet
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

                boolean portfolioUpdated = false;
                try {
                        if (quantity <= 0)
                                throw new IllegalArgumentException("Quantity must be positive");

                        Account account = accountRepository.findByUserIdWithLock(userId)
                                        .orElseThrow(() -> new IllegalStateException("Account not found"));

                        BigDecimal executionPrice = marketPriceListener.getLatestPrice(stockId);
                        if (executionPrice == null) {
                                throw new IllegalStateException(
                                                "Market price not available for stockId " + stockId + ". Please try again.");
                        }

                        BigDecimal totalValue = executionPrice.multiply(BigDecimal.valueOf(quantity));

                        account.creditWallet(totalValue);

                        Trade trade = Trade.create(
                                        userId,
                                        stockId,
                                        TradeType.SELL,
                                        quantity,
                                        executionPrice);
                        trade.setStatus(TradeStatus.PENDING);
                        tradeRepository.save(trade);

                        TradeEvent event = new TradeEvent(
                                        idempotencyKeyStr,
                                        trade.getId(),
                                        userId,
                                        stockId,
                                        quantity,
                                        TradeType.SELL,
                                        executionPrice);

                        kafkaTemplate.send("trade-requests-topic", idempotencyKeyStr, event);

                        TradeResponse response = TradeMapper.toResponse(trade, account.getBalance());

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
