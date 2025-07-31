package com.skytracker.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.SearchContext;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.common.dto.flightSearch.RoundTripFlightSearchResponseDto;
import com.skytracker.mapper.FlightSearchResponseMapper;
import com.skytracker.mapper.RoundTripFlightSearchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class AmadeusResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * Amadeus 항공권 검색 API 응답을 파싱하여 편도 또는 왕복 DTO 리스트로 변환합니다.
     *
     * @param json     Amadeus API의 JSON 응답 문자열
     * @param context  요청 시 고정된 검색 조건 (출발지, 도착지, 인원 등)
     * @return         편도 또는 왕복 항공권 DTO 리스트
     */
    public List<?> parseFlightSearchResponse(String json, SearchContext context) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            Map<String, String> carrierMap = extractCarrierMap(root);

            if (data.isArray() && data.size() > 0) {
                JsonNode firstOffer = data.get(0);
                JsonNode itineraries = firstOffer.get("itineraries");

                if (itineraries != null && itineraries.isArray()) {
                    if (itineraries.size() == 2) {
                        // 왕복 항공권인 경우
                        return StreamSupport.stream(data.spliterator(), false)
                                .map(offer -> toRoundTripDto(offer, carrierMap, context))
                                .collect(Collectors.toList());
                    } else if (itineraries.size() == 1) {
                        // 편도 항공권인 경우
                        return StreamSupport.stream(data.spliterator(), false)
                                .map(offer -> toDto(offer, carrierMap, context))
                                .collect(Collectors.toList());
                    } else {
                        throw new RuntimeException("지원하지 않는 여정 개수입니다: " + itineraries.size());
                    }
                } else {
                    throw new RuntimeException("응답에 'itineraries' 필드가 없거나 잘못된 형식입니다");
                }
            }
            throw new RuntimeException("항공권 데이터가 비어있거나 잘못된 형식입니다");

        } catch (IOException e) {
            throw new RuntimeException("항공권 응답 파싱 중 오류가 발생했습니다", e);
        }
    }

    // --- 책임1: carrierMap 추출 ---
    private Map<String,String> extractCarrierMap(JsonNode root) {
        return objectMapper.convertValue(
                root.at("/dictionaries/carriers"),
                new TypeReference<Map<String,String>>() {});
    }

    // --- 책임2: JsonNode → DTO 변환 메인 로직 (편도) ---
    private FlightSearchResponseDto toDto(JsonNode offer, Map<String,String> carrierMap, SearchContext context) {
        JsonNode segment     = offer.at("/itineraries/0/segments/0");
        JsonNode fareDetails = offer.at("/travelerPricings/0/fareDetailsBySegment/0");
        JsonNode priceNode   = offer.path("price");

        String carrierCode   = segment.path("carrierCode").asText();
        String airlineName   = carrierMap.getOrDefault(carrierCode, "UNKNOWN");
        String flightNumber  = segment.path("number").asText();

        String departureTime = segment.path("departure").path("at").asText();
        String arrivalTime   = segment.path("arrival").path("at").asText();
        String duration      = offer.at("/itineraries/0/duration").asText();

        int seats            = offer.path("numberOfBookableSeats").asInt(0);
        boolean hasCheckedBags = parseCheckedBags(fareDetails);
        boolean isRefundable   = parseFlag(fareDetails, "REFUNDABLE");
        boolean isChangeable   = parseFlag(fareDetails, "CHANGEABLE");

        String currency      = parseCurrency(priceNode);
        int price            = parsePriceValue(priceNode);

        return FlightSearchResponseMapper.toDto(
                carrierCode, airlineName, flightNumber, departureTime,
                arrivalTime, duration, seats, hasCheckedBags,
                isRefundable, isChangeable, currency, price, context
        );
    }

    // --- 책임2: JsonNode → DTO 변환 메인 로직 (왕복) ---
    private RoundTripFlightSearchResponseDto toRoundTripDto(JsonNode offer, Map<String, String> carrierMap, SearchContext context) {
        JsonNode outboundSegment = offer.at("/itineraries/0/segments/0");
        JsonNode returnSegment = offer.at("/itineraries/1/segments/0");
        JsonNode fareDetails = offer.at("/travelerPricings/0/fareDetailsBySegment/0");
        JsonNode priceNode = offer.path("price");

        String carrierCode = outboundSegment.path("carrierCode").asText();
        String airlineName = carrierMap.getOrDefault(carrierCode, "UNKNOWN");
        String flightNumber = outboundSegment.path("number").asText();

        String outboundDepartureTime = outboundSegment.path("departure").path("at").asText();
        String outboundArrivalTime = outboundSegment.path("arrival").path("at").asText();
        String outboundDuration = offer.at("/itineraries/0/duration").asText();

        String returnDepartureTime = returnSegment.path("departure").path("at").asText();
        String returnArrivalTime = returnSegment.path("arrival").path("at").asText();
        String returnDuration = offer.at("/itineraries/1/duration").asText();

        int seats = offer.path("numberOfBookableSeats").asInt(0);
        boolean hasCheckedBags = parseCheckedBags(fareDetails);
        boolean isRefundable = parseFlag(fareDetails, "REFUNDABLE");
        boolean isChangeable = parseFlag(fareDetails, "CHANGEABLE");

        String currency = parseCurrency(priceNode);
        int price = parsePriceValue(priceNode);

        return RoundTripFlightSearchMapper.toDto(
                carrierCode, airlineName, flightNumber,
                outboundDepartureTime, outboundArrivalTime,
                outboundDuration, returnDepartureTime, returnArrivalTime,
                returnDuration, seats, hasCheckedBags, isRefundable,
                isChangeable, currency, price, context
        );
    }

    // --- 책임3: amenities 파싱 및 flag 추출 ---
    private boolean parseFlag(JsonNode fareDetails, String keyword) {
        for (JsonNode amenity : fareDetails.path("amenities")) {
            String desc       = amenity.path("description").asText().toUpperCase();
            boolean chargeable= amenity.path("isChargeable").asBoolean();
            if (!chargeable && desc.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // --- 책임4: 수하물 포함 여부 ---
    private boolean parseCheckedBags(JsonNode fareDetails) {
        return fareDetails
                .path("includedCheckedBags")
                .path("quantity")
                .asInt(0) > 0;
    }

    // --- 책임5: 통화 단위 파싱 ---
    private String parseCurrency(JsonNode priceNode) {
        return priceNode.path("currency").asText();
    }

    // --- 책임6: 가격 값 파싱 ---
    private int parsePriceValue(JsonNode priceNode) {
        String totalStr = priceNode.path("total").asText("0");
        return (int) Float.parseFloat(totalStr);
    }
}