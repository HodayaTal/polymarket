package com.example.polymarket.controller;

import com.example.polymarket.domain.AppUser;
import com.example.polymarket.dto.CreateMarketRequest;
import com.example.polymarket.dto.MarketResponse;
import com.example.polymarket.dto.PricePointResponse;
import com.example.polymarket.dto.ResolveMarketRequest;
import com.example.polymarket.service.AuthService;
import com.example.polymarket.service.MarketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/markets")
public class MarketController {

    private final MarketService marketService;
    private final AuthService authService;

    public MarketController(MarketService marketService, AuthService authService) {
        this.marketService = marketService;
        this.authService = authService;
    }

    @GetMapping
    public List<MarketResponse> list(@RequestParam(required = false) String status) {
        return marketService.list(status);
    }

    @GetMapping("/{id}")
    public MarketResponse get(@PathVariable Long id) {
        return marketService.get(id);
    }

    @GetMapping("/{id}/prices")
    public List<PricePointResponse> priceHistory(@PathVariable Long id) {
        return marketService.history(id);
    }

    @PostMapping
    public MarketResponse create(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateMarketRequest request
    ) {
        authService.requireAdmin(authorization);
        return marketService.create(request);
    }

    @PostMapping("/{id}/close")
    public MarketResponse close(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        authService.requireAdmin(authorization);
        return marketService.close(id);
    }

    @PostMapping("/{id}/cancel")
    public MarketResponse cancel(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        authService.requireAdmin(authorization);
        return marketService.cancel(id);
    }

    @PostMapping("/{id}/resolve")
    public MarketResponse resolve(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ResolveMarketRequest request
    ) {
        AppUser admin = authService.requireAdmin(authorization);
        return marketService.resolve(id, request, admin);
    }
}
