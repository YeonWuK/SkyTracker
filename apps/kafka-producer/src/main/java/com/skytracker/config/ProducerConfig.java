package com.skytracker.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class ProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
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
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, jsonSerializer.getClass());
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return props;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(JsonSerializer<Object> jsonSerializer) {
        return new KafkaTemplate<>(producerFactory(jsonSerializer));
    }
}