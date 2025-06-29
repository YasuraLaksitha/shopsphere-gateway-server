package com.shopsphere.gateway_server.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Bucket4jRedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private String port;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(RedisURI.builder()
                .withHost(host)
                .withPort(Integer.parseInt(port))
                .build());
    }

    @Bean
    public StatefulRedisConnection<String, byte[]> redisConnection(RedisClient redisClient) {
        RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        return redisClient.connect(codec);
    }

    @Bean
    public ProxyManager<String> lettuceBasedProxyManager(StatefulRedisConnection<String, byte[]> redisConnection) {
        return LettuceBasedProxyManager.builderFor(redisConnection)
                .build();
    }
}
