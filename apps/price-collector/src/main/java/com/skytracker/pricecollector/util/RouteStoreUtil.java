package com.skytracker.pricecollector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.service.RedisClient;
import com.skytracker.pricecollector.dto.SortedRouteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteStoreUtil {

    private final RedisClient redisClient;
    private final ObjectMapper objectMapper;

    public void routeStore(List<FlightSearchResponseDto> dtoList) throws JsonProcessingException {
        List<String> rankingList = redisClient.getList(RedisKeys.HOT_ROUTES);

        if (rankingList == null || rankingList.isEmpty()) {
            log.info("HOT_ROUTES 데이터 없음 → 저장 스킵");
            return;
        }

//        Map<String, Integer> minPriceMap = new HashMap<>();

        for (FlightSearchResponseDto responseDto : dtoList) {

            String key = getRouteKey(SortedRouteDto.from(responseDto)); // 변환 필요 시
            if (!rankingList.contains(key)) {
                log.info("Key 미일치 → 저장 스킵: {}", key);
                continue;
            }

            String json = objectMapper.writeValueAsString(responseDto);

            redisClient.kafkaPushList(key, json);
            log.info("HOT ROUTE 저장 성공: {}", key);
        }

//        minPriceMap.forEach((key, value) -> {
//            String minPriceKey = key + ":minPrice";
//            Integer currentMinPrice = redisClient.getminPrice(minPriceKey);
//            if (currentMinPrice == null || value < currentMinPrice) {
//                redisClient.setValueWithTTL(minPriceKey, String.valueOf(value), Duration.ofMinutes(9));
//            }
//        });
    }

    private String getRouteKey(SortedRouteDto dto) {

        String dep = dto.getDepartureAirportCode();
        String arr = dto.getArrivalAirportCode();
        String depDate = dto.getDepartureTime();
        String retDate = dto.getReturnDepartureTime();
        String adults = "1";

        if (retDate == null) {
            return String.join(":", dep, arr, depDate, adults);
        }
        return String.join(":", dep, arr, depDate, retDate, adults);
    }
}