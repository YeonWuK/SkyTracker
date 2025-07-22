package com.skytracker.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class ProducerConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // ✅ LocalDate 등 처리
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ✅ ISO 8601 출력
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); // ✅ private도 포함
        return objectMapper;
    }

    @Bean
    public JsonSerializer<Object> jsonSerializer(ObjectMapper objectMapper) {
        return new JsonSerializer<>(objectMapper);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(JsonSerializer<Object> jsonSerializer) {
        return new DefaultKafkaProducerFactory<>(producerConfigs(jsonSerializer));
    }

    @Bean
    public Map<String, Object> producerConfigs(JsonSerializer<Object> jsonSerializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-broker1:29092,kafka-broker2:29093,kafka-broker3:29094");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, jsonSerializer.getClass());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG, 3);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return props;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(JsonSerializer<Object> jsonSerializer) {
        return new KafkaTemplate<>(producerFactory(jsonSerializer));
    }
}
