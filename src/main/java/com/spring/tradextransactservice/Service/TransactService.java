package com.spring.tradextransactservice.Service;

import com.spring.tradextransactservice.DTO.TradeMapper;
import com.spring.tradextransactservice.DTO.TradeResponse;
import com.spring.tradextransactservice.Enums.TradeType;
import com.spring.tradextransactservice.Models.Account;
import com.spring.tradextransactservice.Models.Trade;
import com.spring.tradextransactservice.Repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final TradeRepository tradeRepository;
    private final MarketHoursValidator marketHoursValidator;
    private final AccountRepository accountRepository;

    @Transactional
    public TradeResponse buyStock(Long userId, String stockSymbol, Integer quantity){

        marketHoursValidator.validateMarketHours();

        if(quantity <= 0 ) throw new IllegalArgumentException("Quantity must be positive");

        User user =  userRepository.findByIdWithLock(userId).
                orElseThrow(() -> new IllegalStateException("User not found"));
        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new IllegalStateException("Stock not found"));

        BigDecimal executionPrice = stock.getCurrentPrice();
        BigDecimal totalCost = executionPrice.multiply(BigDecimal.valueOf(quantity));

        user.debitWallet(totalCost);

        Portfolio portfolio = portfolioRepository
                .findByUserIdAndStockIdWithLock(userId, stock.getId())
                .orElse(Portfolio.createEmptyPortfolio(user, stock));

        portfolio.addHoldings(quantity, executionPrice);

        Trade trade = Trade.create(
                user,stock,TradeType.BUY,
                quantity,executionPrice
        );

        tradeRepository.save(trade);
        return TradeMapper.toResponse(trade, user.getWalletBalance());

    }

    @Transactional
    public TradeResponse sellStock(Long userId, String stockSymbol, Integer quantity){
        if(quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        Account account = accountRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Stock stock  = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new IllegalStateException("Stock not found"));

        BigDecimal executionPrice = stock.getCurrentPrice();

        Portfolio portfolio = portfolioRepository
                .findByUserIdAndStockIdWithLock(userId, stock.getId())
                .orElseThrow(() -> new IllegalStateException("You do not own this portfolio"));

        portfolio.removeHoldings(quantity);

        BigDecimal totalValue = executionPrice.multiply(BigDecimal.valueOf(quantity));

        account.creditWallet(totalValue);

        Trade trade = Trade.create(
                account.getUserId(), stock, TradeType.SELL,
                quantity, executionPrice
        );
        tradeRepository.save(trade);
        return TradeMapper.toResponse(trade, user.getWalletBalance());

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

