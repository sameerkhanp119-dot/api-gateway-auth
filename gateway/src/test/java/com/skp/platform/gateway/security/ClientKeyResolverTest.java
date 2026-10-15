package com.skp.platform.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class ClientKeyResolverTest {

    private final ClientKeyResolver resolver = new ClientKeyResolver();

    @Test
    void usesAuthenticatedClientName() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
                .mutate().principal(Mono.just(new TestingAuthenticationToken("order-portal", null))).build();

        assertThat(resolver.resolve(exchange).block()).isEqualTo("client:order-portal");
    }

    @Test
    void fallsBackToRemoteIp() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/").remoteAddress(new InetSocketAddress("10.0.0.5", 51234)));

        assertThat(resolver.resolve(exchange).block()).isEqualTo("ip:10.0.0.5");
    }
}
