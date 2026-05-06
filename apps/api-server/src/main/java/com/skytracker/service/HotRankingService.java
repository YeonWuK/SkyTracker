package com.skytracker.service;

import com.skytracker.common.dto.HotRouteSummaryDto;
import com.skytracker.common.exception.integrations.HotRouteParsedFailed;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.service.RedisClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotRankingService {

    private final RedisClient redisClient;

    /**
     * HOT_ROUTES 목록을 읽어 인기 노선 요약 DTO 리스트로 변환한다.
     */
    public List<HotRouteSummaryDto> getHotRouteSummary() {
        List<String> keys = redisClient.getList(RedisKeys.HOT_ROUTES);

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<HotRouteSummaryDto> result = new ArrayList<>();

        int rank = 1;
        for (String key : keys) {
            try {
                HotRouteSummaryDto dto = parseUniqueKey(key, rank++);
                result.add(dto);
            } catch (Exception e) {
                log.warn("Invalid route key format: {}", key, e);
            }
        }

        return result;
    }

    /**
     * Redis route key를 화면 응답용 인기 노선 요약 DTO로 변환한다.
     */
    private HotRouteSummaryDto parseUniqueKey(String key, int rank) {

        String[] parts = key.split(":");

        if (parts.length < 4 || parts.length > 5) {
            throw new HotRouteParsedFailed("Invalid key format: " + key);
        }

        String departureAirport = parts[0];
        String arrivalAirport = parts[1];
        String departureDate = parts[2];

        String arrivalDate = (parts.length == 5 ? parts[3] : null);

        int adults = Integer.parseInt(parts.length == 5 ? parts[4] : parts[3]);

        // route key와 함께 저장된 최저가 key를 조회한다.
        String minKey = key + ":minPrice";
        long minPrice = redisClient.getMinPrice(minKey);

        return HotRouteSummaryDto.builder()
                .rank(rank)
                .uniqueKey(key)
                .departureAirportCode(departureAirport)
                .arrivalAirportCode(arrivalAirport)
                .departureDate(departureDate)
                .arrivalDate(arrivalDate)
                .adults(adults)
                .minPrice(minPrice)
                .build();
    }
}
