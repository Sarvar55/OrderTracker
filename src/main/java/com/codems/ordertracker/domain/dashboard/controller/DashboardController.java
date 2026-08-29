package com.codems.ordertracker.domain.dashboard.controller;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import com.codems.ordertracker.domain.base.BaseResponse;
import com.codems.ordertracker.domain.dashboard.dto.DashboardStatsResponse;
import com.codems.ordertracker.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "Aggregated statistics for admins")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping(value = "/stats", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Get dashboard statistics",
            description = "Returns order counts by status and webhook processing success rate. Admin only."
    )
    public ResponseEntity<BaseResponse<DashboardStatsResponse>> stats() {
        return ResponseEntity.ok(BaseResponse.success(dashboardService.getStats()));
    }
}