package com.spring.tradextransactservice.kafka;

import com.spring.tradextransactservice.DTO.TradeMapper;
import com.spring.tradextransactservice.DTO.TradeResponse;
import com.spring.tradextransactservice.Enums.TradeStatus;
import com.spring.tradextransactservice.Enums.TradeType;
import com.spring.tradextransactservice.Models.Account;
import com.spring.tradextransactservice.Models.Trade;
import com.spring.tradextransactservice.Repository.AccountRepository;
import com.spring.tradextransactservice.Repository.TradeRepository;
import com.spring.tradextransactservice.Service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeResultListener {

    private final TradeRepository tradeRepository;
    private final AccountRepository accountRepository;
    private final IdempotencyService idempotencyService;

    @KafkaListener(topics = "trade-responses-topic", groupId = "transact-service-group")
    @Transactional
    public void listen(TradeResultEvent event) {
        log.info("Received TradeResultEvent: {}", event);

        Trade trade = tradeRepository.findById(event.getTradeId()).orElse(null);
        if (trade == null) {
            log.error("Trade not found for ID: {}", event.getTradeId());
            return;
        }

        if (trade.getStatus() != TradeStatus.PENDING) {
            log.warn("Trade {} is already processed. Status: {}", trade.getId(), trade.getStatus());
            return;
        }

        Account account = accountRepository.findByUserIdWithLock(trade.getUserId())
                            .orElseThrow(() -> new IllegalStateException("Account not found"));

        if (event.isSuccess()) {
            trade.setStatus(TradeStatus.COMPLETED);
            tradeRepository.save(trade);
            
            TradeResponse response = TradeMapper.toResponse(trade, account.getBalance());
            idempotencyService.markCompleted(event.getIdempotencyKey(), response);
            log.info("Trade {} completed successfully.", trade.getId());
        } else {
            trade.setStatus(TradeStatus.FAILED);
            // Saga Compensation
            BigDecimal totalValue = trade.getPriceExecuted().multiply(BigDecimal.valueOf(trade.getQuantity()));
            if (trade.getTradeType() == TradeType.BUY) {
                // Refund wallet
                account.creditWallet(totalValue);
            } else if (trade.getTradeType() == TradeType.SELL) {
                // Debit wallet
                account.debitWallet(totalValue);
            }
            accountRepository.save(account);
            tradeRepository.save(trade);
            idempotencyService.markFailed(event.getIdempotencyKey());
            log.info("Trade {} failed. Compensation applied.", trade.getId());
        }
    }
}
