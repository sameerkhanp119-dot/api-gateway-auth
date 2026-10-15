package com.skp.platform.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/** Verifies the scope rules at the edge; requests rejected here never reach a downstream service. */
@SpringBootTest
class GatewaySecurityTest {

    @Autowired
    private ApplicationContext context;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context).apply(springSecurity()).configureClient().build();
    }

    @Test
    void rejectsRequestWithoutToken() {
        client.get().uri("/api/v1/orders/123").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void readOnlyClientCannotPlaceOrders() {
        client.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_orders.read")))
                .post().uri("/api/v1/orders")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void tokenWithoutOrderScopesIsForbidden() {
        client.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_profile")))
                .get().uri("/api/v1/orders/123")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void livenessProbeIsPublic() {
        client.get().uri("/actuator/health/liveness").exchange().expectStatus().isOk();
    }
}
