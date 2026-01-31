package com.shoppeclone.backend.admin.controller;

import com.shoppeclone.backend.admin.dto.response.DashboardStatsResponse;
import com.shoppeclone.backend.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
}
