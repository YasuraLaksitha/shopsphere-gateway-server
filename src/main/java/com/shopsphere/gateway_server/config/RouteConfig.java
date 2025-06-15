package com.shopsphere.gateway_server.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator shopsphereRouteConfig(final RouteLocatorBuilder builder) {
        final String segmentPrefix = "/${segment}";

        final String xResponseTimeHeader = "X-Response-Time";

        return builder.routes()
                .route(p -> p
                        .path("/shopsphere/admins/**")
                        .filters(f -> f.rewritePath("/shopsphere/admins/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString()))
                        .uri("lb://ADMINS"))

                .route(p -> p
                        .path("/shopsphere/users/**")
                        .filters(f -> f.rewritePath("/shopsphere/users/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString()))
                        .uri("lb://USERS"))

                .route(p -> p
                        .path("/shopsphere/products/**")
                        .filters(f -> f.rewritePath("/shopsphere/products/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString()))
                        .uri("lb://PRODUCTS"))

                .route(p -> p
                        .path("/shopsphere/carts/**")
                        .filters(f -> f.rewritePath("/shopsphere/carts/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString()))
                        .uri("lb://CARTS"))

                .route(p -> p
                        .path("/shopsphere/orders/**")
                        .filters(f -> f.rewritePath("/shopsphere/orders/(?<segment>.*)", segmentPrefix)
                                .addResponseHeader(xResponseTimeHeader, LocalDateTime.now().toString()))
                        .uri("lb://ORDERS"))
                .build();
    }
}
