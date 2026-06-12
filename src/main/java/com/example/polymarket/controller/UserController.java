package com.example.polymarket.controller;

import com.example.polymarket.domain.AppUser;
import com.example.polymarket.dto.DashboardResponse;
import com.example.polymarket.dto.UserResponse;
import com.example.polymarket.service.ApiMapper;
import com.example.polymarket.service.AuthService;
import com.example.polymarket.service.DashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;
    private final DashboardService dashboardService;
    private final ApiMapper mapper;

    public UserController(AuthService authService, DashboardService dashboardService, ApiMapper mapper) {
        this.authService = authService;
        this.dashboardService = dashboardService;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        AppUser user = authService.requireUser(authorization);
        return mapper.toUserResponse(user);
    }

    @GetMapping("/me/dashboard")
    public DashboardResponse dashboard(@RequestHeader(name = "Authorization", required = false) String authorization) {
        AppUser user = authService.requireUser(authorization);
        return dashboardService.userDashboard(user);
    }

    @GetMapping("/leaderboard")
    public List<UserResponse> leaderboard() {
        return dashboardService.leaderboard();
    }
}
