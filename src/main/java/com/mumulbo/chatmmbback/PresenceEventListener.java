package com.mumulbo.chatmmbback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * 방(예: /topic/public) 구독 여부로 접속자 수 계산.
 * - 새로고침 시 Disconnect가 Connect보다 늦게 와도,
 *   "구독 세션 집합"만 감소하므로 최종 불일치가 남지 않음.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private final PresenceTracker tracker;
    private final SimpMessagingTemplate messagingTemplate;

    // 필요 시 여러 방을 허용하려면 패턴 확장: /topic/public 또는 /topic/room.*
    private static final String TARGET_DEST = "/topic/public";
    private static final AntPathMatcher matcher = new AntPathMatcher();

    private boolean isTarget(String destination) {
        if (destination == null) return false;
        // 단일 방
        return destination.equals(TARGET_DEST);
        // 다중 방 예시:
        // return destination.equals("/topic/public") || matcher.match("/topic/room.*", destination);
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        String dest = acc.getDestination();
        String sessionId = acc.getSessionId();
        if (!isTarget(dest)) return;

        int count = tracker.onSubscribe(sessionId);
        log.debug("SUBSCRIBE dest={} session={} count={}", dest, sessionId, count);
        broadcast(count);
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = acc.getSessionId();
        // STOMP UNSUBSCRIBE에는 destination이 없을 수 있음 → 세션 단위로 정리
        int count = tracker.onUnsubscribe(sessionId);
        log.debug("UNSUBSCRIBE session={} count={}", sessionId, count);
        broadcast(count);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = acc.getSessionId();
        int count = tracker.onDisconnect(sessionId);
        log.debug("DISCONNECT session={} count={}", sessionId, count);
        broadcast(count);
    }

    private void broadcast(int count) {
        messagingTemplate.convertAndSend("/topic/presence", new PresencePayload("presence", count));
    }

    public record PresencePayload(String type, int count) {}
}
