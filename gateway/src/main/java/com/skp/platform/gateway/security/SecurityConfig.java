package com.skp.platform.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Edge authorization: every API call needs a valid JWT from the auth server, and each
 * route/method maps to an OAuth2 scope. Downstream services never see anonymous traffic.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .pathMatchers("/fallback/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/orders/**").hasAuthority("SCOPE_orders.read")
                        .pathMatchers("/api/v1/orders/**").hasAuthority("SCOPE_orders.write")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> { }))
                .build();
    }
}
