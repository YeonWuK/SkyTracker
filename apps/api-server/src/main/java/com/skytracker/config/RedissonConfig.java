package com.skytracker.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${REDIS_PASSWORD}")
    private String redisPassword;

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
                .setPassword(redisPassword)
                .setSentinelPassword(redisPassword)
                .setDatabase(0);


        return Redisson.create(config);
    }
}