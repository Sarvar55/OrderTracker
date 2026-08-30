package com.codems.ordertracker.domain.webhook.controller;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import com.codems.ordertracker.domain.base.BaseResponse;
import com.codems.ordertracker.domain.base.PageResponse;
import com.codems.ordertracker.domain.webhook.dto.WebhookEventDetailResponse;
import com.codems.ordertracker.domain.webhook.dto.WebhookEventResponse;
import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import com.codems.ordertracker.domain.webhook.service.WebhookLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/logs")
@RequiredArgsConstructor
@Tag(name = "Webhook Logs", description = "Admin endpoints to inspect webhook event history")
public class WebhookLogController {

    private final WebhookLogService webhookLogService;

    @GetMapping(version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "List webhook events",
            description = "Returns a paginated, filterable audit trail of received webhook events. Admin only."
    )
    public ResponseEntity<BaseResponse<PageResponse<WebhookEventResponse>>> findLogs(
            @Parameter(description = "Filter by webhook channel")
            @RequestParam(required = false) WebhookChannel channel,

            @Parameter(description = "Filter by processing status")
            @RequestParam(required = false) WebhookEventStatus status,

            @Parameter(description = "Only events received at or after this timestamp")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "Only events received at or before this timestamp")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(BaseResponse.success(webhookLogService.findLogs(channel, status, from, to, pageable)));
    }

    @GetMapping(value = "/{id}", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Get a webhook event",
            description = "Returns the full payload and processing outcome of a single webhook event. Admin only."
    )
    public ResponseEntity<BaseResponse<WebhookEventDetailResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success(webhookLogService.getById(id)));
    }
}