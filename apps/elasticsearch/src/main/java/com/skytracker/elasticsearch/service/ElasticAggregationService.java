package com.skytracker.elasticsearch.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import com.skytracker.elasticsearch.document.SearchLogsDocument;
import com.skytracker.elasticsearch.dto.ParsedRouteDto;
import com.skytracker.elasticsearch.dto.EsAggregationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticAggregationService {

   private final ElasticsearchOperations operations;

   public List<EsAggregationDto> getTopRoutes(int size) {
      Aggregation topRouteAgg = Aggregation.of(a -> a
              .terms(t -> t
                      .field("routeKey.keyword")
                      .size(size)));

      NativeQuery query = new NativeQueryBuilder()
              .withMaxResults(0)
              .withAggregation("top_routes", topRouteAgg)
              .build();

      SearchHits<SearchLogsDocument> hits = operations.search(query, SearchLogsDocument.class);

      ElasticsearchAggregations aggregations = (ElasticsearchAggregations) hits.getAggregations();

      if (aggregations == null) {
         return List.of();
      }
      ElasticsearchAggregation topRouteAggregation = aggregations.get("top_routes");

      if (topRouteAggregation == null) {
         return List.of();
      }

      Aggregate aggregate = topRouteAggregation.aggregation().getAggregate();

      StringTermsAggregate termsAggregate = aggregate.sterms();

      List<EsAggregationDto> routes = new ArrayList<>();
      for (StringTermsBucket bucket : termsAggregate.buckets().array()) {
         String key = bucket.key().stringValue();
         ParsedRouteDto parsedRouteDto =  parsedRoute(key);

         log.info("집계 결과 - routeKey: {}, docCount: {}, 출발지: {}, 도착지: {}, 출발일: {}, 귀국일: {}",
                 key,
                 bucket.docCount(),
                 parsedRouteDto.getDepartureAirportCode(),
                 parsedRouteDto.getArrivalAirportCode(),
                 parsedRouteDto.getDepartureDate(),
                 parsedRouteDto.getArrivalDate()
         );
         routes.add(EsAggregationDto.from(parsedRouteDto, bucket.docCount()));
      }

      log.info("집계 결과 전체: {}", routes);
      return routes;
   }

   private ParsedRouteDto parsedRoute(String routeKey) {
      String[] parts = routeKey.split("\\|", -1);
      String routeCode     = parts.length > 0 ? parts[0] : "";
      String departureDate = parts.length > 1 ? parts[1] : "";
      String arrivalDate   = parts.length > 2 ? parts[2] : null;

      String[] airports = routeCode.split(":", -1);
      String departureAirportCode = airports.length > 0 ? airports[0] : "";
      String arrivalAirportCode   = airports.length > 1 ? airports[1] : "";

      return new ParsedRouteDto(departureAirportCode, arrivalAirportCode, departureDate, arrivalDate);
   }
}