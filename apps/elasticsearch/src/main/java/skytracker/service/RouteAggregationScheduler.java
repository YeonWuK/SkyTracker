package skytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.RouteAggregationDto;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteAggregationScheduler {

    private final ElasticAggregationService esAggregationService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    /**
     * 매일 01시 실행 - 인기 노선 상위 10개를 Redis에 캐싱
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void updateHotRoutes() throws IOException {
        try {
            List<RouteAggregationDto> topRoutes = esAggregationService.getTopRoute(10);
            topRoutes.sort(Comparator.comparingLong(RouteAggregationDto::getDocCount).reversed());

            redisService.delete(RedisKeys.HOT_ROUTES_TMP);

            for (RouteAggregationDto route : topRoutes) {
                String json = objectMapper.writeValueAsString(route);
                redisService.pushList(RedisKeys.HOT_ROUTES_TMP, json);
            }

            redisService.rename(RedisKeys.HOT_ROUTES_TMP, RedisKeys.HOT_ROUTES);
        } catch (Exception e) {
            log.error("인기 노선 캐싱 실패", e);
        }
    }

}