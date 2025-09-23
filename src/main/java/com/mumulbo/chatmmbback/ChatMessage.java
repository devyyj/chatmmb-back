package com.mumulbo.chatmmbback;

import lombok.Data;

/**
 * 최소 메시지 DTO.
 * - sender: 보낸 사람 표시용
 * - content: 메시지 본문
 */
@Data
public class ChatMessage {
    private String sender;
    private String content;
}
