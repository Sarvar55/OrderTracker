package com.codems.ordertracker.common.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import com.codems.ordertracker.domain.dashboard.controller.DashboardController;
import com.codems.ordertracker.domain.webhook.controller.WebhookController;
import com.codems.ordertracker.domain.webhook.controller.WebhookLogController;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Checks the declared security paths against the mappings the controllers
 * actually expose, so a renamed controller or a typo in a path cannot
 * silently open or close an endpoint.
 */
class SecurityPathsTests {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final SecurityPaths securityPaths = new SecurityPaths();
    private final List<String> publicPaths = securityPaths.publicPaths();
    private final List<String> adminPaths = securityPaths.adminPaths();

    @Test
    @DisplayName("the payment and shipment webhooks are reachable without a token")
    void webhookEndpointsArePublic() {
        String webhooks = basePathOf(WebhookController.class);

        assertThat(isPublic(webhooks + "/payment")).isTrue();
        assertThat(isPublic(webhooks + "/shipment")).isTrue();
    }

    @Test
    @DisplayName("registration and login are reachable without a token")
    void authEndpointsArePublic() {
        assertThat(isPublic(ApplicationConstants.API_PREFIX + "/auth/register")).isTrue();
        assertThat(isPublic(ApplicationConstants.API_PREFIX + "/auth/login")).isTrue();
    }

    @Test
    @DisplayName("the dashboard is admin only")
    void dashboardIsAdminOnly() {
        String stats = basePathOf(DashboardController.class) + "/stats";

        assertThat(isAdmin(stats)).isTrue();
        assertThat(isPublic(stats)).isFalse();
    }

    @Test
    @DisplayName("the webhook log API is admin only through the admin prefix")
    void webhookLogsAreAdminOnly() {
        String logs = basePathOf(WebhookLogController.class);

        assertThat(isAdmin(logs)).isTrue();
        assertThat(isAdmin(logs + "/42")).isTrue();
        assertThat(isPublic(logs)).isFalse();
    }

    @Test
    @DisplayName("customer endpoints are neither public nor admin only")
    void customerEndpointsRequireAuthentication() {
        String orders = ApplicationConstants.API_PREFIX + "/orders";

        assertThat(isPublic(orders)).isFalse();
        assertThat(isAdmin(orders)).isFalse();
    }

    private String basePathOf(Class<?> controller) {
        return ApplicationConstants.API_PREFIX + controller.getAnnotation(RequestMapping.class).value()[0];
    }

    private boolean isPublic(String url) {
        return matchesAny(publicPaths, url);
    }

    private boolean isAdmin(String url) {
        return matchesAny(adminPaths, url);
    }

    private boolean matchesAny(List<String> patterns, String url) {
        return patterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, url));
    }
}
