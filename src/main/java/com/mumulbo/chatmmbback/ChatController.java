package com.mumulbo.chatmmbback;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

@Controller
public class ChatController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage broadcast(ChatMessage message) {
        // 서버에서 신뢰 가능한 타임스탬프/ID 생성
        if (message.getId() == null || message.getId().isBlank()) {
            message.setId(UUID.randomUUID().toString());
        }
        message.setCreatedAt(Instant.now()); // UTC

        // 받은 메시지를 그대로 브로드캐스트(서버 생성 메타 포함)
        return message;
    }
}
