package com.example.polymarket.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
