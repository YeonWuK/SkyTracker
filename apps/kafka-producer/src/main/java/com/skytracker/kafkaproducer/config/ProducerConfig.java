package com.skytracker.kafkaproducer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class ProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private List<String> bootstrapServers;

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
        return new DefaultKafkaProducerFactory<>(producerConfigs(), new StringSerializer(), jsonSerializer);
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        String bootstrapServerString = String.join(",", bootstrapServers);

        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerString);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG, 3);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class.getName());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG, "kafka-producer");
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return props;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(JsonSerializer<Object> jsonSerializer) {
        return new KafkaTemplate<>(producerFactory(jsonSerializer));
    }
}
