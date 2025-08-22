package com.skytracker.elasticsearch.service;

import com.skytracker.elasticsearch.dto.EsAggregationDto;
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

    @Scheduled(cron = "0 0 1 * * *")
    public void updateHotRoutes() {
        try {
            List<EsAggregationDto> topRoutes = esAggregationService.getTopRoutes(10);
            topRoutes.sort(Comparator.comparingLong(EsAggregationDto::getDocCount).reversed());

            redisService.delete(RedisKeys.HOT_ROUTES_TMP);

            int rank = 1;
            for (EsAggregationDto route : topRoutes) {
                String redisKey = RedisKeys.HOT_ROUTES_TMP;
                String field = RedisKeys.HOT_ROUTES + ":" + rank;
                String value = buildValue(route);

                redisService.hashPut(redisKey, field, value);
                rank++;
            }

            redisService.rename(RedisKeys.HOT_ROUTES_TMP, RedisKeys.HOT_ROUTES);

            log.info("인기 노선 캐싱 성공");
        } catch (Exception e) {
            log.error("인기 노선 캐싱 실패", e);
        }
    }

    private String buildValue(EsAggregationDto dto) {
        if (dto.getArrivalDate() == null) {
            return dto.getDepartureAirportCode() + "_" + dto.getArrivalAirport()
                    + "_" + dto.getDepartureDate() + "_" + dto.getAdults()  + "_" + dto.getDocCount();
        }
        return dto.getDepartureAirportCode() + "_" + dto.getArrivalAirport()
                + "_" + dto.getDepartureDate() + "_" + dto.getArrivalDate() + "_" + dto.getAdults() + "_" + dto.getDocCount();
    }
}