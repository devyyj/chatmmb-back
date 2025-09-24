package com.mumulbo.chatmmbback;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 현재 방 구독자 수 반환 */
@RestController
@RequiredArgsConstructor
public class PresenceController {
    private final PresenceTracker tracker;

    @GetMapping("/api/presence/count")
    public CountResponse count() {
        return new CountResponse(tracker.getCount());
    }

    public record CountResponse(int count) {}
}
