package skytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytracker.common.dto.RouteAggregationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteAggregationScheduler {

    private final ElasticAggregationService esAggregationService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String HOT_KEY = "HOT_ROUTES";
    private static final String TEMP_KEY = "HOT_ROUTES:TMP";


    @Scheduled(cron = "0 0 1 * * *")
    public void updateHotRoutes() throws IOException {
        try {
            List<RouteAggregationDto> topRoutes = esAggregationService.getTopRoute(10);
            topRoutes.sort((a, b) -> Long.compare(b.getDocCount(), a.getDocCount()));

            redisTemplate.delete(TEMP_KEY);

            for (RouteAggregationDto route : topRoutes) {
                String json = objectMapper.writeValueAsString(route);
                redisTemplate.opsForList().rightPush(TEMP_KEY, json);
            }

            redisTemplate.rename(TEMP_KEY, HOT_KEY);


        } catch (Exception e) {
            log.error("인기 노선 캐싱 실패", e);
        }
        }

}