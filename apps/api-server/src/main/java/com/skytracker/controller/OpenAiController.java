package com.skytracker.controller;

import com.skytracker.dto.openai.ChatRequestDto;
import com.skytracker.dto.openai.ChatResponseDto;
import com.skytracker.entity.ChatRoom;
import com.skytracker.security.auth.CustomUserDetails;
import com.skytracker.service.openai.ChatMessageService;
import com.skytracker.service.openai.ChatRoomService;
import com.skytracker.service.openai.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OpenAiController {

    private final OpenAiService openAiService;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    /**
     * 현재 로그인한 사용자의 채팅방을 만들거나 조회하고 최근 대화 내역을 반환한다.
     */
    @GetMapping("/chatRoom")
    public List<ChatResponseDto> chatRoom(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long userId = customUserDetails.getUserId();
        ChatRoom chatRoom = chatRoomService.getOrCreate(userId);
        List<ChatResponseDto> history = chatMessageService.getRecentMessages(chatRoom);

        return history;
    }

    /**
     * 사용자의 질문을 OpenAI 서비스에 전달하고 답변을 반환한다.
     */
    @PostMapping("/ask")
    public ChatResponseDto ask(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                      @RequestBody ChatRequestDto chatRequestDto) {
        return openAiService.ask(chatRequestDto.getMessage(), customUserDetails.getUserId());
    }

}
