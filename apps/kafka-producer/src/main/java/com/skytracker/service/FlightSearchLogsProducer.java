package com.skytracker.service;

import com.skytracker.common.dto.SearchLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightSearchLogsProducer {

    private final KafkaTemplate<String, SearchLogDto> kafkaTemplate;

    public void sendSearchLogs(SearchLogDto searchLogDto) {
        kafkaTemplate.send("search-logs", searchLogDto);
    }
}
