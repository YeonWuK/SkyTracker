package skytracker.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import skytracker.document.SearchLogsDocument;

public interface SearchRepository extends ElasticsearchRepository<SearchLogsDocument, Long> {

}
