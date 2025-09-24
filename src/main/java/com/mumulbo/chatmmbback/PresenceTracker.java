package com.mumulbo.chatmmbback;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class PresenceTracker {
    private final Set<String> subscribedSessions = ConcurrentHashMap.newKeySet();
    // presence-key → scheduled removal task
    private final Map<String, ScheduledFuture<?>> pendingRemovals = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "presence-grace");
        t.setDaemon(true);
        return t;
    });

    // 구독 시작: 세션 등록
    public int onSubscribe(String sessionId) {
        subscribedSessions.add(sessionId);
        return subscribedSessions.size();
    }

    public int onUnsubscribe(String sessionId) {
        subscribedSessions.remove(sessionId);
        return subscribedSessions.size();
    }

    public int onDisconnect(String sessionId) {
        subscribedSessions.remove(sessionId);
        return subscribedSessions.size();
    }

    public int getCount() {
        return subscribedSessions.size();
    }

    // === 그레이스 관리 ===
    public void scheduleGraceRemoval(String presenceKey, Runnable removal, long graceMillis) {
        cancelGrace(presenceKey);
        ScheduledFuture<?> f = scheduler.schedule(removal, graceMillis, TimeUnit.MILLISECONDS);
        pendingRemovals.put(presenceKey, f);
    }

    public void cancelGrace(String presenceKey) {
        ScheduledFuture<?> f = pendingRemovals.remove(presenceKey);
        if (f != null) f.cancel(false);
    }
}
