package org.example.bwxw.controller;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.dto.DashboardStats;
import org.example.bwxw.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/stats")
    public ApiResponse<DashboardStats> getStats() {
        try {
            DashboardStats stats = dashboardService.getStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
