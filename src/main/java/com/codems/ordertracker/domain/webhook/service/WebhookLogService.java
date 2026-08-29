package com.codems.ordertracker.domain.webhook.service;

import com.codems.ordertracker.domain.base.PageResponse;
import com.codems.ordertracker.domain.webhook.dto.WebhookEventDetailResponse;
import com.codems.ordertracker.domain.webhook.dto.WebhookEventResponse;
import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEvent;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import com.codems.ordertracker.domain.webhook.exception.WebhookEventNotFoundException;
import com.codems.ordertracker.domain.webhook.mapper.WebhookEventMapper;
import com.codems.ordertracker.domain.webhook.repository.WebhookEventRepository;
import com.codems.ordertracker.domain.webhook.repository.WebhookEventSpecifications;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebhookLogService {

    private final WebhookEventRepository webhookEventRepository;
    private final WebhookEventMapper webhookEventMapper;

    public PageResponse<WebhookEventResponse> findLogs(
            WebhookChannel channel,
            WebhookEventStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        Specification<WebhookEvent> specification = WebhookEventSpecifications.hasChannel(channel);
        specification = and(specification, WebhookEventSpecifications.hasStatus(status));
        specification = and(specification, WebhookEventSpecifications.createdAfter(from));
        specification = and(specification, WebhookEventSpecifications.createdBefore(to));

        Page<WebhookEventResponse> page = webhookEventRepository.findAll(specification, pageable)
                .map(webhookEventMapper::toResponse);

        return PageResponse.from(page);
    }

    public WebhookEventDetailResponse getById(Long id) {
        WebhookEvent event = webhookEventRepository.findById(id)
                .orElseThrow(() -> WebhookEventNotFoundException.byId(id));
        return webhookEventMapper.toDetailResponse(event);
    }

    private Specification<WebhookEvent> and(Specification<WebhookEvent> base, Specification<WebhookEvent> extra) {
        if (base == null) {
            return extra;
        }
        return extra == null ? base : base.and(extra);
    }
}