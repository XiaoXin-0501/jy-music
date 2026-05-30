package com.jy.config;

import com.jy.cache.RedissonService;
import com.jy.service.webSocketService.WebSocketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public boolean initWebSocket(RedissonService redissonService) {
        WebSocketService.setRedisson(redissonService);
        return true;
    }
}
