package com.mumulbo.chatmmbback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private static final String TARGET_DEST = "/topic/public";
    private static final String PRESENCE_KEY_HEADER = "presence-key";
    private static final long GRACE_MS = 30_000; // 30초

    private final PresenceTracker tracker;
    private final SimpMessagingTemplate messagingTemplate;

    private void broadcast(int count) {
        messagingTemplate.convertAndSend("/topic/presence", new PresencePayload("presence", count));
    }

    private String presenceKey(MessageHeaders headers) {
        // nativeHeaders -> presence-key
        Object nativeHeaders = headers.get("nativeHeaders");
        if (nativeHeaders instanceof java.util.Map<?,?> map) {
            Object v = ((java.util.Map<?,?>) nativeHeaders).get(PRESENCE_KEY_HEADER);
            if (v instanceof java.util.List<?> list && !list.isEmpty()) {
                return String.valueOf(list.get(0));
            }
        }
        return null;
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        if (!TARGET_DEST.equals(acc.getDestination())) return;

        // 재접속 복귀: pending grace 취소
        String pkey = presenceKey(acc.getMessageHeaders());
        if (pkey != null) {
            tracker.cancelGrace(pkey);
        }

        int count = tracker.onSubscribe(acc.getSessionId());
        log.debug("SUBSCRIBE public session={} pkey={} count={}", acc.getSessionId(), pkey, count);
        broadcast(count);
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        int count = tracker.onUnsubscribe(acc.getSessionId());
        log.debug("UNSUBSCRIBE session={} count={}", acc.getSessionId(), count);
        broadcast(count);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = acc.getSessionId();
        String pkey = presenceKey(acc.getMessageHeaders());

        // 즉시 감소 대신 그레이스로 지연 제거
        tracker.scheduleGraceRemoval(pkey != null ? pkey : sessionId, () -> {
            int count = tracker.onDisconnect(sessionId);
            log.debug("DISCONNECT(commit) session={} pkey={} count={}", sessionId, pkey, count);
            broadcast(count);
        }, GRACE_MS);

        log.debug("DISCONNECT(schedule) session={} pkey={}", sessionId, pkey);
    }

    public record PresencePayload(String type, int count) {}
}
