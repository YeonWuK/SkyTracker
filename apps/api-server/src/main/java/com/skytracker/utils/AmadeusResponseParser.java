package com.skytracker.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.dto.SearchContext;
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

    public List<FlightSearchResponseDto> parseFlightSearchResponse(String json, SearchContext context) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            Map<String,String> carrierMap = extractCarrierMap(root);

            return StreamSupport.stream(data.spliterator(), false)
                    .map(offer -> toDto(offer, carrierMap, context))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse flight search response", e);
        }
    }

    // --- 책임1: carrierMap 추출 ---
    private Map<String,String> extractCarrierMap(JsonNode root) {
        return objectMapper.convertValue(
                root.at("/data/dictionaries/carriers"),
                new TypeReference<Map<String,String>>() {});
    }

    // --- 책임2: JsonNode → DTO 변환 메인 로직 (origin/destination 제거) ---
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

        return FlightSearchResponseDto.builder()
                .airlineCode(carrierCode)
                .airlineName(airlineName)
                .flightNumber(flightNumber)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .duration(duration)
                .numberOfBookableSeats(seats)
                .hasCheckedBags(hasCheckedBags)
                .isRefundable(isRefundable)
                .isChangeable(isChangeable)
                .currency(currency)
                .price(price)
                .departureAirport(context.originLocationCode())
                .currency(context.currency())
                .departureTime(context.departureDate())
                .build();
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