package com.skp.platform.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    private String runAndCaptureDownstreamId(MockServerWebExchange exchange) {
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstream.set(ex);
            return Mono.empty();
        };
        filter.filter(exchange, chain).block();
        return downstream.get().getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER);
    }

    @Test
    void generatesIdWhenMissingAndEchoesIt() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/orders/1"));

        String forwarded = runAndCaptureDownstreamId(exchange);

        assertThat(forwarded).isNotBlank();
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER)).isEqualTo(forwarded);
    }

    @Test
    void keepsCallerSuppliedId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders/1").header(CorrelationIdFilter.HEADER, "abc-123"));

        assertThat(runAndCaptureDownstreamId(exchange)).isEqualTo("abc-123");
    }

    @Test
    void replacesOversizedId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders/1").header(CorrelationIdFilter.HEADER, "x".repeat(500)));

        assertThat(runAndCaptureDownstreamId(exchange)).hasSizeLessThanOrEqualTo(128);
    }
}
