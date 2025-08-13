package com.skytracker.config;

import com.skytracker.core.config.RedisClusterProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private static final String REDISSON_HOST_PREFIX = "redis://";

    private final RedisClusterProperties redisProps;

    public RedissonConfig(RedisClusterProperties redisProps) {
        this.redisProps = redisProps;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress(redisProps.getNodes().stream()
                        .map(node -> REDISSON_HOST_PREFIX + node)
                        .toArray(String[]::new));
        return Redisson.create(config);
    }

}