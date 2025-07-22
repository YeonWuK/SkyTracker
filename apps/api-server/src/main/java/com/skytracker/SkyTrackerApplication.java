package com.skytracker;

import com.skytracker.config.RedisClusterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableCaching
@EnableScheduling
@EntityScan(basePackages = "com.skytracker.entity")
@EnableJpaRepositories(basePackages = "com.skytracker.repository")
@EnableJpaAuditing
@EnableConfigurationProperties(RedisClusterProperties.class)
@SpringBootApplication
public class SkyTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkyTrackerApplication.class, args);
	}

}
