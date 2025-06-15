package com.shopsphere.gateway_server.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ResponseTraceFilter {

    private final FilterUtil filterUtil;

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) ->
                chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    final HttpHeaders headers = exchange.getRequest().getHeaders();
                    final Optional<String> correlationId = filterUtil.getCorrelationId(headers);

                    correlationId.ifPresent(id -> {
                        log.debug("Updated the correlationId to the outbound headers: {}", id);
                        exchange.getResponse().getHeaders().add(FilterUtil.CORRELATION_ID, id);
                    });
                }));
    }
}
