package com.mumulbo.chatmmbback;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * 클라이언트 → 서버:  /app/chat.send 로 전송
 * 서버 → 구독자:     /topic/public 로 브로드캐스트
 */
@Controller
public class ChatController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage broadcast(ChatMessage message) {
        // 최소 처리: 받은 메시지를 그대로 브로드캐스트
        return message;
    }
}
