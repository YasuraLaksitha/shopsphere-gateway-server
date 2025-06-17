package com.shopsphere.gateway_server.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@Component
public class FilterUtil {

    public static final String CORRELATION_ID = "shopsphere-correlation-id";

    public static final String USER_ID_HEADER = "X-User-Id";

    public Optional<String> getCorrelationId(final HttpHeaders requestHeaders) {
        return Optional.ofNullable(requestHeaders.getFirst(CORRELATION_ID));
    }

    public ServerWebExchange setRequestHeader(final ServerWebExchange exchange, final String name, final String value) {

        if (CORRELATION_ID.equals(name) &&
                !exchange.getRequest().getHeaders().containsKey(CORRELATION_ID)) {
            return exchange.mutate()
                    .request(builder -> builder.header(name, value))
                    .build();

        } else if (USER_ID_HEADER.equals(name) &&
                !exchange.getRequest().getHeaders().containsKey(USER_ID_HEADER)) {

            return exchange.mutate()
                    .request(builder -> builder.header(name, value))
                    .build();
        }
        return exchange;
    }

    public ServerWebExchange setCorrelationId(final ServerWebExchange exchange, final String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }

    public ServerWebExchange setUserId(ServerWebExchange exchange, final String userId) {

        if (exchange.getRequest().getHeaders().containsKey(USER_ID_HEADER)) {
            exchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .headers(headers -> headers.remove(USER_ID_HEADER))
                            .build())
                    .build();
        }
        return this.setRequestHeader(exchange, USER_ID_HEADER, userId);
    }
}
