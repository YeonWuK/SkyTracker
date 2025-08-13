package com.skytracker.elasticsearch.repository;

import com.skytracker.elasticsearch.document.SearchLogsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository extends ElasticsearchRepository<SearchLogsDocument, Long> {

}
