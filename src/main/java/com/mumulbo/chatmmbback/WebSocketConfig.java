package com.mumulbo.chatmmbback;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1) 순수 WebSocket(STOMP) 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // 필요시 특정 오리진으로 제한

        // 2) SockJS 전용 엔드포인트
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
