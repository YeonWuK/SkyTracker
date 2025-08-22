package com.skytracker.elasticsearch.repository;

import com.skytracker.elasticsearch.document.CommonLogsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonLogRepository extends ElasticsearchRepository<CommonLogsDocument, String> {
}
