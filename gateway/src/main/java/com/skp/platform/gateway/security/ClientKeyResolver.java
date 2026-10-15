package com.skp.platform.gateway.security;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.security.Principal;

/**
 * Rate-limit bucket key. Authenticated calls are limited per OAuth2 client (the token subject),
 * so one noisy integration cannot starve the others; anything else falls back to client IP.
 */
@Component("clientKeyResolver")
public class ClientKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(Principal::getName)
                .map(name -> "client:" + name)
                .switchIfEmpty(Mono.fromSupplier(() -> "ip:" + remoteIp(exchange)));
    }

    private static String remoteIp(ServerWebExchange exchange) {
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        if (address == null || address.getAddress() == null) {
            return "unknown";
        }
        return address.getAddress().getHostAddress();
    }
}
