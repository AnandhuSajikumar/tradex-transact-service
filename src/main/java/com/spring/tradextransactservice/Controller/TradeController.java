package com.spring.tradextransactservice.Controller;

import com.spring.tradextransactservice.DTO.TradeRequest;
import com.spring.tradextransactservice.DTO.TradeResponse;
import com.spring.tradextransactservice.UserDetails.UserPrincipal;
import com.spring.tradextransactservice.Service.TransactService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@Getter
@RequiredArgsConstructor
@RequestMapping("api/v1/trade")
public class TradeController {
    private final TransactService transactService;


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/buy")
    public TradeResponse buy(
            @RequestBody TradeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        return transactService.buyStock(
                userPrincipal.getId(),
                request.getStockSymbol(),
                request.getQuantity()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all-trades")
    public Page<TradeResponse> allTrades(@PageableDefault(size = 5) Pageable pageable) {
        return transactService.getAllTrade(pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/orders")
    public Page<TradeResponse> tradeHistoryById(
            @PageableDefault(size = 5) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        return transactService.getTradeHistory(userPrincipal.getId(), pageable);
    }



}
