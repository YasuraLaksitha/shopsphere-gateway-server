package com.shopsphere.gateway_server.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                        log.debug(" Failed to decode JWT: {}", e.getMessage());

                        return Mono.empty();
                    });
        }
        return Mono.empty();
    }

    public Mono<List<String>> extractRolesFromAuthHeader(final String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            final String token = authHeader.substring(BEARER_PREFIX.length());

            return reactiveJwtDecoder.decode(token).map(jwt -> {
                final Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

                if (realmAccess == null || realmAccess.isEmpty())
                    return new ArrayList<>();
                return ((List<?>) realmAccess.get("roles"))
                        .stream()
                        .map(Object::toString)
                        .toList();
            });
        }
        return Mono.just(new ArrayList<>());
    }
}
