package com.skytracker.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        SentinelServersConfig sentinelConfig = config.useSentinelServers()
                .setMasterName("mymaster")
                .addSentinelAddress(
                        "redis://my-redis-node-0.my-redis-headless.data.svc.cluster.local:26379",
                        "redis://my-redis-node-1.my-redis-headless.data.svc.cluster.local:26379",
                        "redis://my-redis-node-2.my-redis-headless.data.svc.cluster.local:26379"
                )
                .setPassword("redis");

        return Redisson.create(config);
    }
}