package com.skytracker.service;

import com.skytracker.dto.FlightSearchRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmadeusFlightSearchService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private static final String FLIGHTSERACH_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";

    public String searchFlights(String accessToken, FlightSearchRequestDto req) {
        String url = buildFlightSearchUrl(req);
        ResponseEntity<String> response = callAmadeusGetApi(url, accessToken);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Flight search result = {}", response.getBody());
//            redisTemplate.opsForHash().put(accessToken, response.getBody());
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to search flights");
        }
    }

    private String buildFlightSearchUrl(FlightSearchRequestDto req) {
        return UriComponentsBuilder.fromHttpUrl(FLIGHTSERACH_URL)
                .queryParam("originLocationCode", req.getOriginLocationCode())
                .queryParam("destinationLocationCode", req.getDestinationLocationCode())
                .queryParam("departureDate", req.getDepartureDate())
                .queryParam("adults", req.getAdults())
                .queryParam("nonStop", req.isNonStop())
                .queryParam("travelClass", req.getTravelClass().getValue())
                .queryParam("currencyCode", req.getCurrencyCode())
                .queryParam("max", req.getMax())
                .toUriString();
    }

    private ResponseEntity<String> callAmadeusGetApi(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }
}
