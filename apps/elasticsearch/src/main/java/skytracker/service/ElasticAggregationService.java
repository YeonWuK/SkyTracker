package skytracker.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregationSource;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.skytracker.common.dto.RouteAggregationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticAggregationService {

    private final ElasticsearchClient esClient;

    public List<RouteAggregationDto> getTopRoute(int size) throws IOException {
        SearchRequest request = AggregateRequest(size);
        SearchResponse<Void> response = esClient.search(request, Void.class);

        return response.aggregations()
                .get("top_routes")
                .composite()
                .buckets()
                .array()
                .stream()
                .map(bucket -> {
                    Map<String, FieldValue> keyMap = bucket.key();
                    return new RouteAggregationDto(
                            keyMap.get("routeCode").toString(),
                            keyMap.get("departureDate").toString(),
                            keyMap.get("returnDate") != null ? keyMap.get("returnDate").toString() : null,
                            bucket.docCount()
                    );
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }


    private SearchRequest AggregateRequest(int size) {
        List<Map<String, CompositeAggregationSource>> sources = List.of(
                Map.of("routeCode", CompositeAggregationSource.of(s -> s.terms(t -> t.field("routeCode.keyword")))),
                Map.of("departureDate", CompositeAggregationSource.of(s -> s.terms(t -> t.field("departureDate")))),
                Map.of("returnDate", CompositeAggregationSource.of(s -> s.terms(t -> t.field("returnDate"))))
        );

        Aggregation agg = Aggregation.of(a -> a
                .composite(c -> c
                        .size(size)
                        .sources(sources))
        );

        return SearchRequest.of(r -> r
                .index("search-log")
                .size(0)
                .aggregations("top_routes", agg)
                .query(q -> q.bool(b -> b
                        .must(m -> m.range(rg -> rg
                                .field("timestamp")
                                .gte(JsonData.fromJson("now-1d"))
                                .lte(JsonData.fromJson("now"))
                        ))
                        .must(m -> m.range(rg -> rg
                                .field("departureDate")
                                .gte(JsonData.fromJson("now"))
                                .lte(JsonData.fromJson("now-+3d")))))));
    }
}