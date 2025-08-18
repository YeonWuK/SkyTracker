package com.skytracker.pricealert.config.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.support.KafkaHeaders.*;

@Slf4j
@Component
public class ConsumerErrorsHandler {
    public void postProcessDltMessage(ConsumerRecord<Long, Object> record,
                                      @Header(RECEIVED_TOPIC) String receivedTopic,
                                      @Header(RECEIVED_PARTITION) int partitionId,
                                      @Header(OFFSET) Long offset,
                                      @Header(GROUP_ID) String groupId) {
        log.error("[DLT Log] received message ='{}' with partitionId ='{}', offset ='{}', topic ='{}', groupId ='{}'",
                record.value(), partitionId, offset, receivedTopic, groupId);
    }
}