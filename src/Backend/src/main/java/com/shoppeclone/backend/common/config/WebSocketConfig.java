package com.shoppeclone.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import lombok.RequiredArgsConstructor;
import com.shoppeclone.backend.auth.security.WebSocketAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cho phép server gửi tin nhắn đến các client đăng ký vào đường dẫn bắt đầu bằng /topic
        config.enableSimpleBroker("/topic");
        // Prefix cho các message gửi từ client lên server (nếu dùng @MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint để client khởi tạo kết nối WebSocket (vd: new SockJS('/ws/chat'))
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*") // Hỗ trợ CORS
                .withSockJS(); // Cung cấp fallback HTTP nếu trình duyệt không hỗ trợ WebSocket
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Thêm Interceptor để chặn và xác thực Token trước khi cho phép kết nối
        registration.interceptors(webSocketAuthInterceptor);
    }
}
