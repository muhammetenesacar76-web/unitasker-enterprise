package com.example.unitaskerbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    // 🛡️ GÜVENLİK YAMASI: İçeri gelen tüm Canlı Yayın (WebSocket) isteklerini denetleyen filtre
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Eğer kullanıcı ilk defa canlı yayına bağlanmaya çalışıyorsa (CONNECT aşaması)
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> authorization = accessor.getNativeHeader("Authorization");

                    // Başlıkta Token yoksa veya Bearer ile başlamıyorsa BAĞLANTIYI KES! (Anonim sızma engellenir)
                    if (authorization == null || authorization.isEmpty() || !authorization.get(0).startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Error: WebSocket Authentication Failed! Token missing.");
                    }

                    String token = authorization.get(0).substring(7);

                    // Buraya kendi JwtUtil sınıfındaki doğrulama metodunu ekleyebilirsin:
                    // if (!jwtUtil.validateToken(token)) { throw new IllegalArgumentException("Token invalid"); }

                    System.out.println("WebSocket üzerinden güvenli bağlantı sağlandı.");
                }
                return message;
            }
        });
    }
}