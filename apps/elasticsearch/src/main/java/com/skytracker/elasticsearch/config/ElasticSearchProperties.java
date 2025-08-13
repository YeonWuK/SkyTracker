package com.skytracker.elasticsearch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.elasticsearch")
public class ElasticSearchProperties {
    private String uris;
    private String username;
    private String password;
}