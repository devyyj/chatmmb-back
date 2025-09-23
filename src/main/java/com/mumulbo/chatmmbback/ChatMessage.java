package com.mumulbo.chatmmbback;

import lombok.Data;
import java.time.Instant;

/**
 * 메시지 DTO
 * - id: 서버가 생성(중복/정렬 안정성)
 * - userId/sender/content: 클라이언트 전달값
 * - createdAt: 서버 생성 시각(UTC)
 * - clientSentAt: 클라이언트 낙관적 타임스탬프(옵션, 수신 시 표시 대체용)
 */
@Data
public class ChatMessage {
    private String id;
    private String userId;
    private String sender;
    private String content;
    private Instant createdAt;
    private Instant clientSentAt; // 선택: 클라이언트가 보낼 수 있음
}
