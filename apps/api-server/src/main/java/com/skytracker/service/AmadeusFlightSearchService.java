package com.skytracker.service;

import com.skytracker.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.dto.SearchContext;
import com.skytracker.utils.AmadeusResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmadeusFlightSearchService {

//    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final AmadeusResponseParser parser;
    private static final String FLIGHTSERACH_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";

    /**
    /  redis 에서 조회한 accessToken 과 req 통해 Amadeus API 에 Response 요청
     */
    public List<FlightSearchResponseDto> searchFlights(String accessToken, FlightSearchRequestDto req) {
        try {
            String url = buildFlightSearchUrl(req);
            ResponseEntity<String> response = callAmadeusGetApi(url, accessToken);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to search flights");
            }

            log.info("Search flights response: {}", response.getBody());

            SearchContext ctx = new SearchContext(
                    req.getAdults(),
                    req.getOriginLocationCode(),
                    req.getDestinationLocationCode(),
                    req.getCurrencyCode(),
                    req.getDepartureDate());

            return parser.parseFlightSearchResponse(response.getBody(), ctx);
        } catch (HttpServerErrorException e) {
            log.error("Amadeus 서버 내부 오류: {}", e.getResponseBodyAsString());
            throw new RuntimeException ("현재 항공권 조회가 불가능합니다. 잠시 후 다시 시도해주세요.");
        }

    }

    /**
    /   URL 에 담아서 보낼 요청 값들 build
     */
    private String buildFlightSearchUrl(FlightSearchRequestDto req) {
        return UriComponentsBuilder.fromHttpUrl(FLIGHTSERACH_URL)
                .queryParam("originLocationCode", req.getOriginLocationCode())
                .queryParam("destinationLocationCode", req.getDestinationLocationCode())
                .queryParam("departureDate", req.getDepartureDate())
                .queryParam("arrivalDate", req.getReturnDate())
                .queryParam("adults", req.getAdults())
                .queryParam("nonStop", req.isNonStop())
                .queryParam("travelClass", req.getTravelClass().getValue())
                .queryParam("currencyCode", req.getCurrencyCode())
                .queryParam("max", req.getMax())
                .toUriString();
    }

    /**
     *  요청 url 값 확인 및 accessToken 확인. 이후 Amadeus API 에다 GET 요청
     */
    private ResponseEntity<String> callAmadeusGetApi(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);
        log.info("✅ 요청 URL: {}", url);
        log.info("✅ 요청 AccessToken: {}", accessToken);
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }

//    public int compareFlightsPrice(FlightSearchRequestDto dto) {
//
//    }
}
