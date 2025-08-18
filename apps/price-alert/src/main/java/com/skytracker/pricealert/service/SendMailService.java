package com.skytracker.pricealert.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import com.skytracker.pricealert.dto.SendMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class SendMailService {

    private final JavaMailSender mailSender;

    public void sendMail(FlightAlertEventMessageDto eventMessageDto) {
        try {
            SimpleMailMessage smm = new SimpleMailMessage();
            SendMessageDto message = SendMessageDto.from(eventMessageDto);

            if (message.getEmail() == null || !message.getEmail().contains("@")) {
                throw new IllegalArgumentException("잘못된 이메일 주소 형식: " + message.getEmail());
            }

            smm.setTo(message.getEmail());
            smm.setSubject(message.getSubject());
            smm.setText(message.getContent());

            mailSender.send(smm);
            log.info("이메일 {}로 알림 발송 완료", message.getEmail());
        } catch (IllegalArgumentException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("메일 발송 중 오류 발생: " + e.getMessage(), e);
        }
    }
}