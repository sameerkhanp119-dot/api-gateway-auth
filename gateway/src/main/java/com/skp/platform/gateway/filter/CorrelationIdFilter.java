package com.skp.platform.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Ensures every request carries an X-Correlation-Id, passes it to the downstream service and
 * echoes it back to the caller, so one id ties together logs across the gateway and services.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";
    private static final int MAX_LENGTH = 128;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
        String correlationId = isUsable(incoming) ? incoming : UUID.randomUUID().toString();

        ServerWebExchange mutated = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(HEADER, correlationId)))
                .build();
        mutated.getResponse().getHeaders().set(HEADER, correlationId);
        return chain.filter(mutated);
    }

    /** Rejects blank or oversized client-supplied ids so they cannot bloat logs or headers. */
    private static boolean isUsable(String value) {
        return value != null && !value.isBlank() && value.length() <= MAX_LENGTH;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
