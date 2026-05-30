package com.jy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 核心：允许所有域名（解决allowCredentials=true不能用*的问题）
        config.addAllowedOriginPattern("*");

        // 允许携带凭证（Cookie、JWT等）
        config.setAllowCredentials(true);

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有HTTP方法（GET/POST/PUT/DELETE/OPTIONS等）
        config.addAllowedMethod("*");

        // 预检请求有效期（3600秒，避免频繁OPTIONS请求）
        config.setMaxAge(3600L);

        // 2. 配置生效路径（所有接口）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 3. 返回CORS过滤器
        return new CorsFilter(source);
    }
}
