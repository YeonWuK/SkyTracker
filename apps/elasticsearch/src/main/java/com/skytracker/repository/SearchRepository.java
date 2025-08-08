package skytracker.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import skytracker.document.SearchLogsDocument;

@Repository
public interface SearchRepository extends ElasticsearchRepository<SearchLogsDocument, Long> {

}
