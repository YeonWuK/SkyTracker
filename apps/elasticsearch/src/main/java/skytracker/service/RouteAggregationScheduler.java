package skytracker.service;

import com.skytracker.common.dto.RouteAggregationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteAggregationScheduler {

    private final ElasticAggregationService esAggregationService;

    @Scheduled(cron = "0 0 1 * * *")
    public void updateHotRoutes() throws IOException {
        List<RouteAggregationDto> topRoutes = esAggregationService.getTopRoute(10);
    }
}
