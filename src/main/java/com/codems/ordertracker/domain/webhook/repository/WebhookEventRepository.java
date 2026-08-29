package com.codems.ordertracker.domain.webhook.repository;

import com.codems.ordertracker.domain.webhook.entity.WebhookChannel;
import com.codems.ordertracker.domain.webhook.entity.WebhookEvent;
import com.codems.ordertracker.domain.webhook.entity.WebhookEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookEventRepository
        extends JpaRepository<WebhookEvent, Long>, JpaSpecificationExecutor<WebhookEvent> {

    boolean existsByChannelAndProviderEventId(WebhookChannel channel, String providerEventId);

    long countByStatus(WebhookEventStatus status);
}