package com.shoppeclone.backend.admin.controller;

import com.shoppeclone.backend.admin.service.SalesCounterSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/maintenance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMaintenanceController {

    private final SalesCounterSyncService salesCounterSyncService;

    @PostMapping("/sync-sales-counters")
    public ResponseEntity<Map<String, Object>> syncSalesCounters() {
        return ResponseEntity.ok(salesCounterSyncService.syncSalesCounters());
    }
}
