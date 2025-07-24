package com.skytracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.data.redis.cluster")
public class RedisClusterProperties {
    private List<String> nodes;
}
