package com.skytracker.pricecollector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.common.dto.flightSearch.RoundTripFlightSearchResponseDto;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.service.RedisService;
import com.skytracker.pricecollector.dto.SortedRouteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RouteStoreUtil {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public void routeStore(List<Object> dtoList) throws JsonProcessingException {

        Map<Object,Object> rankingMap = redisService.getHash(RedisKeys.HOT_ROUTES);

        for (Object dto: dtoList) {
            if (dto instanceof FlightSearchResponseDto responseDto) {

                String json = objectMapper.writeValueAsString(responseDto);

                SortedRouteDto sortedRouteDto = SortedRouteDto.from(responseDto);

                String routeKey = getRouteKey(sortedRouteDto);

                String matchedRankField =  rankingMap.entrySet().stream()
                        .filter(e -> String.valueOf(e.getValue()).startsWith(routeKey + "_"))
                        .map(e -> String.valueOf(e.getKey()))
                        .findFirst()
                        .orElse(null);

                redisService.pushList(matchedRankField, json);

            } else if (dto instanceof RoundTripFlightSearchResponseDto responseDto) {

                String json = objectMapper.writeValueAsString(responseDto);

                SortedRouteDto sortedRouteDto = SortedRouteDto.from(responseDto);

                String routeKey = getRouteKey(sortedRouteDto);

                String matchedRankField =  rankingMap.entrySet().stream()
                        .filter(e -> String.valueOf(e.getValue()).startsWith(routeKey + "_"))
                        .map(e -> String.valueOf(e.getKey()))
                        .findFirst()
                        .orElse(null);

                redisService.pushList(matchedRankField, json);
            }
        }
    }

    private String getRouteKey(SortedRouteDto dto) {

        String departureAirport = dto.getDepartureAirportCode();
        String arrivalAirport = dto.getArrivalAirportCode();
        String departureTime = dto.getDepartureTime();
        String arrivalTime = dto.getArrivalTime();

        return (dto.getArrivalTime() == null)
                ? String.join("_", departureAirport, arrivalAirport, departureTime)
                : String.join("_", departureAirport, arrivalAirport, departureTime, arrivalTime);
    }
}
