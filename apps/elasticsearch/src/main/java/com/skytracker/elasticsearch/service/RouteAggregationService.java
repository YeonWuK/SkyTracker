package com.skytracker.elasticsearch.service;

import com.skytracker.elasticsearch.dto.EsAggregationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteAggregationService {

    private final ElasticAggregationService esAggregationService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 1 * * *")
    public void updateHotRoutes() {
        try {
            List<EsAggregationDto> topRoutes = esAggregationService.getTopRoutes(10);
            topRoutes.sort(Comparator.comparingLong(EsAggregationDto::getDocCount).reversed());

            redisService.delete(RedisKeys.HOT_ROUTES_TMP);

            for (EsAggregationDto route : topRoutes) {
                String json = objectMapper.writeValueAsString(route);
                redisService.pushList(RedisKeys.HOT_ROUTES_TMP, json);
            }

            redisService.rename(RedisKeys.HOT_ROUTES_TMP, RedisKeys.HOT_ROUTES);

            log.info("인기 노선 캐싱 성공");
        } catch (Exception e) {
            log.error("인기 노선 캐싱 실패", e);
        }
    }
}