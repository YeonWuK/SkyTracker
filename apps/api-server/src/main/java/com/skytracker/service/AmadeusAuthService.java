package com.skytracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmadeusAuthService {

    @Value("${spring.amadeus.api.client-id}")
    private String clientId;

    @Value("${spring.amadeus.api.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    public String getAccessToken() {
        String url = "https://test.api.amadeus.com/v1/security/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String token = (String) response.getBody().get("access_token");
            log.info("accessToken = {}", token);
            return token;
        } else {
            throw new RuntimeException("Failed to get access token");
        }
    }

    public String searchFlights(String accessToken) {
        String url = UriComponentsBuilder.fromHttpUrl("https://test.api.amadeus.com/v2/shopping/flight-offers")
                .queryParam("originLocationCode", "ICN")
                .queryParam("destinationLocationCode", "LHR")
                .queryParam("departureDate", "2025-07-15")
                .queryParam("adults", 1)
                .queryParam("nonStop", true)
                .queryParam("max", 3)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Flight search result = {}", response.getBody());
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to search flights");
        }
    }
}
