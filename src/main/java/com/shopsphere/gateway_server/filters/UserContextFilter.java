package com.shopsphere.gateway_server.filters;

import com.shopsphere.gateway_server.utils.FilterUtil;
import com.shopsphere.gateway_server.utils.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(2)
@RequiredArgsConstructor
public class UserContextFilter implements GlobalFilter {

    private final FilterUtil filterUtil;

    private final JWTUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        return jwtUtil.extractUserIdFromAuthHeader(authHeader)
                .map(userId -> filterUtil.setUserId(exchange, userId))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }
}
