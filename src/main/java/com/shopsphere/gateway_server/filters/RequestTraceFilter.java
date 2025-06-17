package com.shopsphere.gateway_server.filters;

import com.shopsphere.gateway_server.utils.FilterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Order(1)
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestTraceFilter implements GlobalFilter {

    private final FilterUtil filterUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final HttpHeaders headers = exchange.getRequest().getHeaders();

        if (isCorrelationIdPresent(headers))
            log.debug("shopsphere-correlation-id found in RequestTraceFilter: {}", filterUtil.getCorrelationId(headers));
        else {
            final String correlationId = generateCorrelationId();
            exchange = filterUtil.setCorrelationId(exchange, correlationId);
            log.debug("shopsphere-correlation-id generated in RequestTraceFilter: {}", correlationId);
        }

        return chain.filter(exchange);
    }

    private boolean isCorrelationIdPresent(final HttpHeaders headers) {
        return filterUtil.getCorrelationId(headers).isPresent();
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
