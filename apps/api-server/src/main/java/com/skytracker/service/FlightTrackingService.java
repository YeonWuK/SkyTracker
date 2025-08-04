package com.skytracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.RouteAggregationDto;
import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.mapper.FlightSearchResponseMapper;
import com.skytracker.core.service.AmadeusFlightSearchService;
import com.skytracker.core.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightTrackingService {

    private final FlightPriceUpdateProducer flightPriceUpdateProducer;
    private final AmadeusFlightSearchService amadeusService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    /**
     * 가격 수집 및 가격변동 이벤트 발행 (9분)
     */
    @Scheduled(cron = "0 */9 * * * *")
    public void collectAndPublishPrices() {
        try {
            String accessToken = redisService.getValue(RedisKeys.AMADEUS_TOKEN);
            List<RouteAggregationDto> cachedHotRoutes = getCachedHotRoutes();

            int totalPublished = 0;

            for (RouteAggregationDto route : cachedHotRoutes) {
                FlightSearchRequestDto req = FlightSearchResponseMapper.toFlightSearchRequestDto(route);

                List<?> responses = amadeusService.searchFlights(accessToken, req);
                for (Object responseDto : responses) {
                    flightPriceUpdateProducer.sendPriceUpdate(responseDto);
                    totalPublished++;
                }
            }
            log.info("항공권 가격 수집 및 Kafka 발행 완료 ({}건)", totalPublished);
        } catch (Exception e) {
            log.error("항공권 가격 수집 및 Kafka 발행 실패", e);
        }
    }

    /**
     * Redis 에서 상위 인기 노선 10개 Get
     */
    private List<RouteAggregationDto> getCachedHotRoutes() throws JsonProcessingException {
        List<Object> rawList = redisService.getList(RedisKeys.HOT_ROUTES);
        List<RouteAggregationDto> result = new ArrayList<>();
        for (Object json : rawList) {
            result.add(objectMapper.readValue(json.toString(), RouteAggregationDto.class));
        }
        return result;
    }
}