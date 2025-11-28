package com.skytracker.service;

import com.skytracker.common.dto.HotRouteSummaryDto;
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

    private HotRouteSummaryDto parseUniqueKey(String key, int rank) {

        String[] parts = key.split(":");

        if (parts.length < 4 || parts.length > 5) {
            throw new IllegalArgumentException("Invalid key format: " + key);
        }

        String minPriceKey = key + ":minPrice";
        Integer minPrice = redisClient.getminPrice(minPriceKey);

        String departureAirport = parts[0];
        String arrivalAirport = parts[1];
        String departureDate = parts[2];

        String arrivalDate = (parts.length == 5 ? parts[3] : null);

        int adults = Integer.parseInt(parts.length == 5 ? parts[4] : parts[3]);

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

//    /**
//     * Redis에 저장된 JSON 리스트(String)를 FlightSearchResponseDto 리스트로 변환
//     */
//    private List<FlightSearchResponseDto> classifyDto(List<String> values) throws JsonProcessingException {
//        List<FlightSearchResponseDto> result = new ArrayList<>();
//
//        try {
//            for (String value : values) {
//                if (value == null) {
//                    continue;
//                }
//
//                JsonNode node = objectMapper.readTree(value);
//
//                // tripType 필터 (필요 없으면 이 블록 제거해도 됨)
//                String tripType = node.path("tripType").asText();
//                if (!"ONE_WAY".equals(tripType) && !"ROUND_TRIP".equals(tripType)) {
//                    // 이상한 값이면 스킵
//                    continue;
//                }
//
//                FlightSearchResponseDto dto =
//                        objectMapper.treeToValue(node, FlightSearchResponseDto.class);
//
//                result.add(dto);
//            }
//        } catch (JsonMappingException e) {
//            throw new JsonMappingFailedException("역직렬화 실패: JSON 구조가 DTO와 맞지 않습니다", e);
//        }
//
//        return result;
//    }