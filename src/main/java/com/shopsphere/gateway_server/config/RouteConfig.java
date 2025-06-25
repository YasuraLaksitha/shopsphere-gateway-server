package com.shopsphere.gateway_server.config;

import com.shopsphere.gateway_server.utils.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    public static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final JWTUtil jwtUtil;

    private final PerRoleRateLimiter rateLimiter;

    @Bean
    public RouteLocator shopsphereRouteConfig(final RouteLocatorBuilder builder) {
        final String segmentPrefix = "/${segment}";

        final String xResponseTimeHeader = "X-Response-Time";

        return builder.routes()
                .route(p -> p
                        .path("/shopsphere/admins/**")
                        .filters(f -> f.rewritePath("/shopsphere/admins/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString())
                                .requestRateLimiter(config -> {
                                    config.setKeyResolver(this.roleBasedKeyResolver());
                                    config.setRateLimiter(rateLimiter);
                                }))
                        .uri("lb://ADMINS"))

                .route(p -> p
                        .path("/shopsphere/users/**")
                        .filters(f -> f.rewritePath("/shopsphere/users/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString())
                                .requestRateLimiter(config -> {
                                    config.setKeyResolver(this.roleBasedKeyResolver());
                                    config.setRateLimiter(rateLimiter);
                                }))
                        .uri("lb://USERS"))

                .route(p -> p
                        .path("/shopsphere/products/**")
                        .filters(f -> f.rewritePath("/shopsphere/products/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString())
                                .requestRateLimiter(config -> {
                                    config.setKeyResolver(this.roleBasedKeyResolver());
                                    config.setRateLimiter(rateLimiter);
                                }))
                        .uri("lb://PRODUCTS"))

                .route(p -> p
                        .path("/shopsphere/carts/**")
                        .filters(f -> f.rewritePath("/shopsphere/carts/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString())
                                .requestRateLimiter(config -> {
                                    config.setKeyResolver(this.roleBasedKeyResolver());
                                    config.setRateLimiter(rateLimiter);
                                }))
                        .uri("lb://CARTS"))

                .route(p -> p
                        .path("/shopsphere/orders/**")
                        .filters(f -> f.rewritePath("/shopsphere/orders/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString())
                                .requestRateLimiter(config -> {
                                    config.setKeyResolver(this.roleBasedKeyResolver());
                                    config.setRateLimiter(rateLimiter);
                                }))
                        .uri("lb://ORDERS"))
                .build();
    }

    @Bean
    public KeyResolver roleBasedKeyResolver() {
        return exchange -> {
            final HttpHeaders headers = exchange.getRequest().getHeaders();

            final List<String> authHeader = headers.get(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && !authHeader.isEmpty()) {
                return jwtUtil.extractRolesFromAuthHeader(authHeader.getFirst())
                        .flatMap(roles -> {
                                    final Mono<String> userIdMono = jwtUtil.extractUserIdFromAuthHeader(authHeader.getFirst());

                                    if (roles.contains("ADMIN"))
                                        return userIdMono.map(userId -> RATE_LIMIT_PREFIX + userId + ":ADMIN");

                                    if (roles.contains("SELLER"))
                                        return userIdMono.map(userId -> RATE_LIMIT_PREFIX + userId + ":SELLER");

                                    return userIdMono.map(userId -> RATE_LIMIT_PREFIX + userId + ":BUYER");
                                }
                        );
            }
            final String host = headers.getFirst(HttpHeaders.HOST);
            final String userAgent = headers.getFirst(HttpHeaders.USER_AGENT);

            return StringUtils.hasText(host) && StringUtils.hasText(userAgent) ?
                    Mono.just(host + ":" + userAgent) : Mono.empty();
        };
    }
}
