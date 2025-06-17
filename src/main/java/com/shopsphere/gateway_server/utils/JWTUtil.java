package com.shopsphere.gateway_server.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTUtil {

    private final ReactiveJwtDecoder reactiveJwtDecoder;

    private static final String BEARER_PREFIX = "Bearer ";

    public Mono<String> extractUserIdFromAuthHeader(final String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            final String token = authHeader.substring(BEARER_PREFIX.length());
            return reactiveJwtDecoder.decode(token)
                    .map(jwt -> {
                        final String userId = jwt.getClaimAsString("sub");
                        log.debug("Extracted userId from JWT: {}", userId);
                        return userId;
                    })
                    .onErrorResume(e -> {
                        log.warn(" Failed to decode JWT: {}", e.getMessage());
                        return Mono.empty();
                    });
        }
        return Mono.empty();
    }
}
