package backend.skytracker.controller;

import backend.skytracker.service.AmadeusAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TestController {

    private final AmadeusAuthService authService;

    @GetMapping("/test")
    public void test() {
        String accessToken = authService.getAccessToken();

        String flightInfo = authService.searchFlights(accessToken);
        log.info(flightInfo);
    }

}
