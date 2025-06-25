package com.shopsphere.gateway_server.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Primary
@Slf4j
public class PerRoleRateLimiter implements RateLimiter<NoopRateLimiterConfig> {

    public static final String ADMIN_ROLE = "ADMIN";

    public static final String SELLER_ROLE = "SELLER";

    public static final String BUYER_ROLE = "BUYER";

    public static final String GUST_ROLE = "GUEST";

    private final ProxyManager<String> proxyManager;

    private Map<String, Bandwidth> roleBandwidths;

    private Bucket fallbackBucket;

    @PostConstruct
    public void init() {
        roleBandwidths = new ConcurrentHashMap<>();

        roleBandwidths.put(ADMIN_ROLE, BandwidthBuilder.builder().capacity(100).refillGreedy(100, Duration.ofMinutes(1)).build());
        roleBandwidths.put(SELLER_ROLE, BandwidthBuilder.builder().capacity(10).refillGreedy(10, Duration.ofMinutes(1)).build());
        roleBandwidths.put(BUYER_ROLE, BandwidthBuilder.builder().capacity(10).refillGreedy(10, Duration.ofMinutes(1)).build());
        roleBandwidths.put(GUST_ROLE, BandwidthBuilder.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build());

        fallbackBucket = Bucket.builder()
                .addLimit(BandwidthBuilder.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                .build();
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        final String representation = id.substring(id.lastIndexOf(":") + 1);
        final Bandwidth bandwidth = roleBandwidths.getOrDefault(representation, roleBandwidths.get(GUST_ROLE));

        log.debug("Rate limiting check initiated for routeId='{}', id='{}', resolvedRole='{}'", routeId, id, representation);

        try {
            final Bucket bucket = proxyManager.builder().build(id, () ->
                    BucketConfiguration.builder()
                            .addLimit(bandwidth)
                            .build()
            );

            final boolean consumed = bucket.tryConsume(1);
            final long tokensRemaining = bucket.getAvailableTokens();

            if (consumed)
                log.info("Request allowed for id='{}' (role='{}'). Tokens remaining: {}", id, representation, tokensRemaining);
            else
                log.warn("Rate limit exceeded for id='{}' (role='{}'). Request blocked. Tokens remaining: {}", id, representation, tokensRemaining);

            return Mono.just(new Response(consumed, this.getHeaders(bucket, bandwidth, representation)));
        } catch (Exception e) {
            log.warn("Redis unavailable or error occurred. Falling back to local bucket for id: {}", id, e);

            boolean consumed = fallbackBucket.tryConsume(1);
            return Mono.just(new Response(consumed, getHeaders(fallbackBucket, bandwidth, representation)));
        }
    }

    private Map<String, String> getHeaders(final Bucket bucket, final Bandwidth bandwidth, final String role) {
        final HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
        headerMap.put("X-RateLimit-Capacity", String.valueOf(bandwidth.getCapacity()));
        headerMap.put("X-RateLimit-Role", role);

        return headerMap;
    }

    @Override
    public Map<String, NoopRateLimiterConfig> getConfig() {
        return Map.of();
    }

    @Override
    public Class<NoopRateLimiterConfig> getConfigClass() {
        return NoopRateLimiterConfig.class;
    }

    @Override
    public NoopRateLimiterConfig newConfig() {
        return new NoopRateLimiterConfig();
    }
}
