package com.skytracker.pricealert.config;

import com.skytracker.pricealert.config.handler.ConsumerErrorsHandler;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.support.EndpointHandlerMethod;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${KAFKA_BROKER_SERVERS}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>(Object.class);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "kafka-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "price-alert");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3);
        factory.setAutoStartup(false);

        return factory;
    }

        @Bean
        public RetryTopicConfiguration retryTopicConfig(KafkaTemplate<String, Object> kafkaTemplate) {
            return RetryTopicConfigurationBuilder
                    .newInstance()
                    .includeTopic("flight-alert")
                    .autoCreateTopicsWith(KafkaConsumerProperties.REPLICATION_FACTOR, KafkaConsumerProperties.REPLICA_COUNT)
                    .maxAttempts(KafkaConsumerProperties.MAX_ATTEMPT_COUNT)
                    .fixedBackOff(KafkaConsumerProperties.BACKOFF_PERIOD)
                    .listenerFactory(kafkaListenerContainerFactory())
                    .dltSuffix(".DLT")
                    .dltHandlerMethod(new EndpointHandlerMethod(ConsumerErrorsHandler.class, "postProcessDltMessage"))
                    .create(kafkaTemplate);
        }
}