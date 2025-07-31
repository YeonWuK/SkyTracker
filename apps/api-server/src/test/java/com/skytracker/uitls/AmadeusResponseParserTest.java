package com.skytracker.uitls;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.SearchContext;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.utils.AmadeusResponseParser;
import org.junit.jupiter.api.Test;

import java.util.List;

class AmadeusResponseParserTest {

    @Test
    void parseFlightSearchResponse_ShouldWorkProperly() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        AmadeusResponseParser parser = new AmadeusResponseParser(objectMapper);

        String json = """
                {"meta":{"count":1,"links":{"self":"https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode=ICN&destinationLocationCode=LHR&departureDate=2025-07-25&adults=1&nonStop=true&travelClass=ECONOMY&currencyCode=KRW&max=1"}},"data":[{"type":"flight-offer","id":"1","source":"GDS","instantTicketingRequired":false,"nonHomogeneous":false,"oneWay":false,"isUpsellOffer":false,"lastTicketingDate":"2025-07-21","lastTicketingDateTime":"2025-07-21","numberOfBookableSeats":9,"itineraries":[{"duration":"PT14H25M","segments":[{"departure":{"iataCode":"ICN","terminal":"2","at":"2025-07-25T10:55:00"},"arrival":{"iataCode":"LHR","terminal":"4","at":"2025-07-25T17:20:00"},"carrierCode":"KE","number":"907","aircraft":{"code":"77W"},"operating":{"carrierCode":"KE"},"duration":"PT14H25M","id":"1","numberOfStops":0,"blacklistedInEU":false}]}],"price":{"currency":"KRW","total":"1118800.00","base":"1050000.00","fees":[{"amount":"0.00","type":"SUPPLIER"},{"amount":"0.00","type":"TICKETING"}],"grandTotal":"1118800.00","additionalServices":[{"amount":"178600","type":"CHECKED_BAGS"}]},"pricingOptions":{"fareType":["PUBLISHED"],"includedCheckedBagsOnly":true},"validatingAirlineCodes":["KE"],"travelerPricings":[{"travelerId":"1","fareOption":"STANDARD","travelerType":"ADULT","price":{"currency":"KRW","total":"1118800.00","base":"1050000.00"},"fareDetailsBySegment":[{"segmentId":"1","cabin":"ECONOMY","fareBasis":"EHX00RMK","brandedFare":"EYSTANDARD","brandedFareLabel":"ECONOMY STANDARD","class":"E","includedCheckedBags":{"quantity":1},"includedCabinBags":{"quantity":1},"amenities":[{"description":"MEAL","isChargeable":false,"amenityType":"MEAL","amenityProvider":{"name":"BrandedFare"}},{"description":"REFUNDABLE TICKET","isChargeable":true,"amenityType":"BRANDED_FARES","amenityProvider":{"name":"BrandedFare"}},{"description":"CHANGEABLE TICKET","isChargeable":true,"amenityType":"BRANDED_FARES","amenityProvider":{"name":"BrandedFare"}}]}]}]}],"dictionaries":{"locations":{"ICN":{"cityCode":"SEL","countryCode":"KR"},"LHR":{"cityCode":"LON","countryCode":"GB"}},"aircraft":{"77W":"BOEING 777-300ER"},"currencies":{"KRW":"S.KOREAN WON"},"carriers":{"KE":"KOREAN AIR"}}}
                """;

        SearchContext ctx = new SearchContext(
                1, "ICN", "LHR", "KRW", "2025-07-25"
        );

        // when
        List<?> result = parser.parseFlightSearchResponse(json, ctx);

        // then
        assertThat(result).isNotEmpty();
        System.out.println("✅ 파싱된 결과: " + result.get(0));
    }
}
