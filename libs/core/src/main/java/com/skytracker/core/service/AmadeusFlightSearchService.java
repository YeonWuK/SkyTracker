package com.skytracker.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.SearchContext;
import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.common.exception.BusinessException;
import com.skytracker.common.exception.integrations.FlightPriceComparisonException;
import com.skytracker.common.exception.integrations.FlightSearchException;
import com.skytracker.core.utils.AmadeusResponseParser;
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
     * Redis에서 조회한 accessToken으로 Amadeus 항공권 검색 API를 호출한다.
     */
    public List<FlightSearchResponseDto> searchFlights(String accessToken, FlightSearchRequestDto req) {
        long start = System.nanoTime();
        try {
            Map<String, Object> requestBody = buildFlightSearchRequestBody(req);
            ResponseEntity<String> response = callAmadeusPostApi(requestBody, accessToken);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to search flights");
            }

            SearchContext ctx = new SearchContext(
                    req.getAdults(),
                    req.getOriginLocationAirport(),
                    req.getDestinationLocationAirport(),
                    req.getTravelClass()
            );

            List<FlightSearchResponseDto> offers = parser.parseFlightSearchResponse(response.getBody(), ctx);

            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.info("✈️ FlightSearch: {}→{} / {} / 성인 {}명 → {}건 ({} ms)",
                    req.getOriginLocationAirport(),
                    req.getDestinationLocationAirport(),
                    req.getDepartureDate(),
                    req.getAdults(),
                    offers.size(),
                    tookMs
            );

            return offers;

        } catch (HttpServerErrorException e) {
            log.error("Amadeus 서버 내부 오류: {}", e.getResponseBodyAsString());
            throw new FlightSearchException("Amadeus 서버 오류: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("항공권 검색 중 예외", e);
            throw new FlightSearchException(e.getMessage(), e);
        }
    }

    /**
     * Amadeus Flight Offers API에 전달할 POST 요청 본문을 생성한다.
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
        outbound.put("destinationLocationCode", req.getDestinationLocationAirport());
        Map<String, String> outboundTime = Map.of("date", req.getDepartureDate());
        outbound.put("departureDateTimeRange", outboundTime);
        originDestList.add(outbound);

        // 왕복인 경우 귀국 구간 추가 (방향 반대로)
        if (req.getReturnDate() != null && !req.getReturnDate().isBlank()) {
            Map<String, Object> inbound = new HashMap<>();
            inbound.put("id", "2");
            inbound.put("originLocationCode", req.getDestinationLocationAirport());   // 반대 방향
            inbound.put("destinationLocationCode", req.getOriginLocationAirport());
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

        // 직항만 조회하고 싶을 때 Amadeus 필터 세팅
        if (req.isNonStop()) {
            Map<String, Object> flightFilters = new HashMap<>();
            Map<String, Object> connectionRestriction = new HashMap<>();
            connectionRestriction.put("maxNumberOfConnections", 0);
            flightFilters.put("connectionRestriction", connectionRestriction);
            searchCriteria.put("flightFilters", flightFilters);
        }

        body.put("searchCriteria", searchCriteria);

        return body;
    }

    /**
     * 공통 헤더를 붙여 Amadeus Flight Offers API에 POST 요청을 보낸다.
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

    /**
     * 알림 조건에 해당하는 항공권을 다시 조회해 현재 최저가를 반환한다.
     */
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

             // 조회 결과가 없으면 가격 비교 실패로 처리한다.
             JsonNode data = root.path("data");
             if (!data.isArray() || data.isEmpty()) {
                 throw new FlightPriceComparisonException("항공권 가격을 찾을 수 없습니다.");
             }

             // 4. 최소 가격 추출 (여러 옵션 중 가장 싼 거)
             String priceStr = data.get(0).path("price").path("total").asText();
             int price = (int) Double.parseDouble(priceStr);

             log.info("조회된 new 항공권 가격: {}", price);
             return price;

         } catch (BusinessException e) {
             // 이미 커스텀 예외인 경우 ErrorCode를 유지한 채 상위 핸들러로 전달한다.
             throw e;
         } catch (Exception e) {
             log.error("가격 비교 중 오류", e);
             throw new FlightPriceComparisonException("항공권 가격 비교 중 오류 발생", e);
         }
     }
}
