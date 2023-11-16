package top.wml.message.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic","/user");// 使用内置的消息代理，消息的目的地前缀是"/topic"
        registry.setApplicationDestinationPrefixes("/app"); // 定义消息前缀，所有发送到"/app"前缀的消息都会路由到带有@MessageMapping的方法上
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*"); // 定义WebSocket端点，客户端将使用这个端点来连接。使用SockJS为了确保在不支持WebSocket的浏览器上仍然可以工作。
    }
}
