package com.example.polymarket.controller;

import com.example.polymarket.dto.AdminDashboardResponse;
import com.example.polymarket.service.AuthService;
import com.example.polymarket.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;
    private final DashboardService dashboardService;

    public AdminController(AuthService authService, DashboardService dashboardService) {
        this.authService = authService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard(@RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return dashboardService.adminDashboard();
    }
}
