package com.skytracker.pricealert.dto;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageDto {

    private String email;

    private String subject;

    private String content;

    public static SendMessageDto from(FlightAlertEventMessageDto eventMessageDto) {

        String subject = "항공권 가격 하락 알림";
        String content =
                "<h3>항공권 가격 하락 알림</h3>" +
                        "<p>출발지: <b>" + eventMessageDto.getDepartureAirport() + "</b></p>" +
                        "<p>도착지: <b>" + eventMessageDto.getArrivalAirport() + "</b></p>" +
                        "<p>출발일: <b>" + eventMessageDto.getDepartureDate() + "</b></p>" +
                        (eventMessageDto.getArrivalDate() != null ? "<p>귀국일: <b>" + eventMessageDto.getArrivalDate() + "</b></p>" : "") +
                        "<p>기존 항공가: " + eventMessageDto.getLastCheckedPrice() + "원</p>" +
                        "<p>변경된 항공가: <span style='color:red;font-weight:bold;'>" + eventMessageDto.getNewPrice() + "원</span></p>" +
                        "<br/>" +
                        "<p>SkyTracker 서비스에서 확인하세요!</p>";

        return builder()
                .email(eventMessageDto.getEmail())
                .subject(subject)
                .content(content)
                .build();
    }
}
