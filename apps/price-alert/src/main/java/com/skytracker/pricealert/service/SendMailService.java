package com.skytracker.pricealert.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import com.skytracker.common.exception.user.EmailInvalidException;
import com.skytracker.common.exception.user.EmailSendFailedException;
import com.skytracker.pricealert.dto.SendMessageDto;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class SendMailService {

    private final JavaMailSender mailSender;

    public void sendMail(FlightAlertEventMessageDto eventMessageDto) {
        SendMessageDto message = SendMessageDto.from(eventMessageDto);
        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            if (message.getEmail() == null || !message.getEmail().contains("@")) {
                throw new EmailInvalidException(message.getEmail());
            }

            messageHelper.setTo(message.getEmail());
            messageHelper.setSubject(message.getSubject());
            messageHelper.setText(message.getContent(), true);

            mailSender.send(mimeMessage);
            log.info("이메일 알림 발송 완료");
        } catch (Exception e) {
            throw new EmailSendFailedException(message.getEmail(), e);
        }
    }
}
