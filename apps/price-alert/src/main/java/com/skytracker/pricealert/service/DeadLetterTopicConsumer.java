package com.skytracker.pricealert.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeadLetterTopicConsumer {

    private final SendMailService sendMailService;

    @KafkaListener(topics = "flight-alert.DLT", groupId = "flight-alert-DLT-group")
    public void onMessage(FlightAlertEventMessageDto eventMessageDto, Acknowledgment ack) {
        sendMailService.sendMail(eventMessageDto);

        ack.acknowledge();
    }
}