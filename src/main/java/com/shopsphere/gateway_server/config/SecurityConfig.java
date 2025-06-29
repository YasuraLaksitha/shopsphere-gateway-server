package com.shopsphere.gateway_server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";

    private static final String BUYER = "BUYER";

    private final KeycloakRoleConverter keycloakRoleConverter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity security) {
        return security.authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/shopsphere/products/**").permitAll()
                        .pathMatchers("/shopsphere/*/swagger-ui/**", "/shopsphere/*/v3/api-docs/**").permitAll()
                        .pathMatchers("/shopsphere/carts/api/contact-info/**").permitAll()
                        .pathMatchers("/shopsphere/users/api/public/**").permitAll()
                        .pathMatchers("/shopsphere/orders/webhook/stripe").permitAll()
                        .pathMatchers("/shopsphere/admins/**").hasRole(ADMIN)
                        .pathMatchers("/shopsphere/users/api/user/**").hasAnyRole(BUYER, ADMIN)
                        .pathMatchers("/shopsphere/carts/api/user/**").hasAnyRole(BUYER, ADMIN)
                        .pathMatchers("/shopsphere/orders/api/user/**").hasAnyRole(BUYER, ADMIN)
                        .pathMatchers("/shopsphere/shipping/**").hasRole(ADMIN)
                        .pathMatchers("/shopsphere/payments/**").hasRole(ADMIN)
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(grantedAuthorityExtractor())))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    public Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthorityExtractor() {
        final JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(authenticationConverter);
    }
}
