package com.mumulbo.chatmmbback;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 방 구독 기반 접속자 추적.
 * - 같은 세션이 여러 번 SUBSCRIBE해도 1로 계산(세션 단위 집합)
 * - UNSUBSCRIBE/DISCONNECT 시에만 감소
 */
@Component
public class PresenceTracker {
    // 현재 방을 구독 중인 세션 ID 집합
    private final Set<String> subscribedSessions = ConcurrentHashMap.newKeySet();

    /** 구독 시작(해당 세션이 처음 구독이면 추가) */
    public int onSubscribe(String sessionId) {
        subscribedSessions.add(sessionId);
        return subscribedSessions.size();
    }

    /** 구독 종료(세션이 실제로 구독 중일 때만 제거) */
    public int onUnsubscribe(String sessionId) {
        subscribedSessions.remove(sessionId);
        return subscribedSessions.size();
    }

    /** 세션 종료 시 정리(구독 중이던 세션만 제거) */
    public int onDisconnect(String sessionId) {
        subscribedSessions.remove(sessionId);
        return subscribedSessions.size();
    }

    public int getCount() {
        return subscribedSessions.size();
    }
}
