package com.example.polymarket.controller;

import com.example.polymarket.domain.AppUser;
import com.example.polymarket.dto.TradeRequest;
import com.example.polymarket.dto.TradeResponse;
import com.example.polymarket.service.AuthService;
import com.example.polymarket.service.TradeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final AuthService authService;
    private final TradeService tradeService;

    public TradeController(AuthService authService, TradeService tradeService) {
        this.authService = authService;
        this.tradeService = tradeService;
    }

    @PostMapping("/trades")
    public TradeResponse trade(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody TradeRequest request
    ) {
        AppUser user = authService.requireUser(authorization);
        return tradeService.execute(user, request);
    }

    @GetMapping("/trades/recent")
    public List<TradeResponse> recentTrades() {
        return tradeService.recentTrades();
    }

    @GetMapping("/markets/{marketId}/trades/recent")
    public List<TradeResponse> recentTradesForMarket(@PathVariable Long marketId) {
        return tradeService.recentTradesForMarket(marketId);
    }
}
