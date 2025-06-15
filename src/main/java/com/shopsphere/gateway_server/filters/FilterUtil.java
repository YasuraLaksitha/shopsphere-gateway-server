package com.shopsphere.gateway_server.filters;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;
import java.util.Optional;

@Component
public class FilterUtil {

    public static final String CORRELATION_ID = "shopsphere-correlation-id";

    public Optional<String> getCorrelationId(final HttpHeaders requestHeaders) {
        return Optional.ofNullable(requestHeaders.getFirst(CORRELATION_ID));
    }

    public ServerWebExchange setRequestHeader(final ServerWebExchange exchange, final String name, final String value) {
        return Objects.isNull(exchange.getRequest().getHeaders().get(CORRELATION_ID)) ?
                exchange.mutate().request(exchange.getRequest().mutate().header(name, value).build()).build() :
                null;
    }

    public ServerWebExchange setCorrelationId(final ServerWebExchange exchange, final String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }
}
