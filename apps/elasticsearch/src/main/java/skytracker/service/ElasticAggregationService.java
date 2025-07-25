package skytracker.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.skytracker.dto.RouteAggregationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticAggregationService {

    private final ElasticsearchClient esClient;

    public List<RouteAggregationDto> getTopRoute(int size) throws IOException {
        SearchRequest request = AggregateRequest(size);
        SearchResponse<Void> response = esClient.search(request, Void.class);

        List<RouteAggregationDto> result = response.aggregations()
                .get("top_routes")
                .sterms()
                .buckets()
                .array()
                .stream()
                .map(bucket -> {
                    String[] parts = bucket.key().stringValue().split(":");
                    if (parts.length != 4) {
                        log.info("Aggregation Parsing Error {}", bucket.key().stringValue());
                        return null;
                    }
                    String routeCode = parts[0] + ":" + parts[1];
                    String departureDate = parts[2];
                    String returnDate = Objects.equals(parts[3], "null") ? null : parts[3];
                    return new RouteAggregationDto(routeCode, departureDate, returnDate, bucket.docCount());
                })
                .filter(Objects::nonNull)
                .toList();

        return result;
    }


    private SearchRequest AggregateRequest(int size) {
        Aggregation agg = AggregationBuilders
                .terms(t -> t
                        .field("composite_key.keyword")
                        .size(size));

        return SearchRequest.of(req -> req
                .index("search-log")
                .size(0)
                .query(q -> q.range(r -> r
                        .field("timestamp")
                        .gte(JsonData.of("now-1d"))
                        .lte(JsonData.of("now"))
                ))
                .aggregations("top_routes", agg)
        );
    }
}