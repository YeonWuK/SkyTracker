package com.skytracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.SearchContext;
import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.utils.AmadeusResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmadeusFlightSearchService {

    private final RestTemplate restTemplate;
    private final AmadeusResponseParser parser;
    private static final String FLIGHTSERACH_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";

    /**
     * redis 에서 조회한 accessToken 과 req 통해 Amadeus API 에 Response 요청
     */
    public List<?> searchFlights(String accessToken, FlightSearchRequestDto req) {
        try {
            Map<String, Object> requestBody = buildFlightSearchRequestBody(req);
            ResponseEntity<String> response = callAmadeusPostApi(requestBody, accessToken);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to search flights");
            }

            log.info("Search flights response: {}", response.getBody());

            SearchContext ctx = new SearchContext(
                    req.getAdults(),
                    req.getOriginLocationAirport(),
                    req.getDestinationLocationAirPort(),
                    req.getTravelClass()
                    );

            return parser.parseFlightSearchResponse(response.getBody(), ctx);
        } catch (HttpServerErrorException e) {
            log.error("Amadeus 서버 내부 오류: {}", e.getResponseBodyAsString());
            throw new RuntimeException("현재 항공권 조회가 불가능합니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * POST 요청용 Request Body 생성
     */
    private Map<String, Object> buildFlightSearchRequestBody(FlightSearchRequestDto req) {
        Map<String, Object> body = new HashMap<>();

        // 통화 코드
        body.put("currencyCode", "KRW");

        // 편도
        List<Map<String, Object>> originDestList = new ArrayList<>();
        Map<String, Object> outbound = new HashMap<>();
        outbound.put("id", "1");
        outbound.put("originLocationCode", req.getOriginLocationAirport());
        outbound.put("destinationLocationCode", req.getDestinationLocationAirPort());
        Map<String, String> outboundTime = Map.of("date", req.getDepartureDate());
        outbound.put("departureDateTimeRange", outboundTime);
        originDestList.add(outbound);

        //왕복
        if (req.getReturnDate() != null && !req.getReturnDate().isBlank()) {
            Map<String, Object> inbound = new HashMap<>();
            inbound.put("id", "2");
            inbound.put("originLocationCode", req.getOriginLocationAirport());
            inbound.put("destinationLocationCode", req.getDestinationLocationAirPort());
            Map<String, String> inboundTime = Map.of("date", req.getReturnDate());
            inbound.put("departureDateTimeRange", inboundTime);
            originDestList.add(inbound);
        }
        body.put("originDestinations", originDestList);


        // travelers 구성
        Map<String, Object> traveler = new HashMap<>();
        traveler.put("id", "1");
        traveler.put("travelerType", "ADULT"); // 또는 CHILD, SENIOR, etc
        body.put("travelers", List.of(traveler));

        // sources
        body.put("sources", List.of("GDS"));

        // 검색 조건
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("maxFlightOffers", req.getMax());
        body.put("searchCriteria", searchCriteria);

        return body;
    }

    /**
     * Amadeus Flight Offers API POST 호출
     */
    private ResponseEntity<String> callAmadeusPostApi(Map<String, Object> body, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("✅ POST 요청 URL: {}", FLIGHTSERACH_URL);
        log.info("✅ 요청 Body: {}", body);
        log.info("✅ 요청 AccessToken: {}", accessToken);

        return restTemplate.exchange(
                FLIGHTSERACH_URL,
                HttpMethod.POST,
                request,
                String.class
        );
    }

     public int compareFlightsPrice(String accessToken, FlightAlertRequestDto dto) {
         try {
             FlightSearchRequestDto searchReq = dto.toSearchRequest();
             // 1. 요청 본문 구성 및 전송
             Map<String, Object> requestBody = buildFlightSearchRequestBody(searchReq);
             ResponseEntity<String> response = callAmadeusPostApi(requestBody, accessToken);

             // 2. 응답 JSON 문자열
             String body = response.getBody();

             // 3. JSON 파싱
             ObjectMapper objectMapper = new ObjectMapper();
             JsonNode root = objectMapper.readTree(body);

             JsonNode data = root.path("data");
             if (!data.isArray() || data.isEmpty()) {
                 throw new RuntimeException("항공권 가격을 찾을 수 없습니다.");
             }

             // 4. 최소 가격 추출 (여러 옵션 중 가장 싼 거)
             String priceStr = data.get(0).path("price").path("total").asText();
             int price = (int) Double.parseDouble(priceStr);

             log.info("조회된 new 항공권 가격: {}", price);
             return price;

         } catch (Exception e) {
             log.error("가격 비교 중 오류", e);
             throw new RuntimeException("항공권 가격 비교 중 오류 발생");
         }
     }
}