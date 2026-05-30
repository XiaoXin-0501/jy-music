package com.jy.config;

import com.jy.filter.JwtAuthenticationTokenFilter;
import com.jy.handle.LogoutSuccessHandlerImpl;
import com.jy.handle.SecurityExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * spring security配置
 *
 * @author ruoyi
 */
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig {
    /**
     * 自定义用户认证逻辑
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 异常处理类
     */
    @Autowired
    private SecurityExceptionHandler securityExceptionHandler;

    /**
     * 退出处理类
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * token认证过滤器
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * 跨域过滤器
     */
    @Autowired
    private CorsFilter corsFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder：Spring官方推荐，不可逆加密，自动生成盐值，无需手动处理
        return new BCryptPasswordEncoder();
    }

    /**
     * 身份验证实现
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 禁用CSRF：前后端分离项目无session，CSRF防护无意义，强制开启会导致POST/PUT/DELETE请求失败
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        // 禁用页面缓存（防止敏感数据被浏览器缓存）
                        .cacheControl(cache -> cache.disable())
                        // 允许iframe同域嵌入（如项目中使用Druid监控、Swagger嵌套）
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .exceptionHandling(exception -> exception
                        // 未认证/令牌过期/令牌无效时的异常处理器（返回自定义JSON）
                        .authenticationEntryPoint(securityExceptionHandler)
                        // 已认证但权限不足时的异常处理器（返回自定义JSON）
                        .accessDeniedHandler(securityExceptionHandler)
                )
                .authorizeHttpRequests(auth -> auth
                                // ① 完全匿名访问
                                .requestMatchers("/login", "/captcha").permitAll()
                                .requestMatchers("/ws/**", "/exception/**", "/song/getCollectRank", "/song/getPlayRank", "/song/getHotRank", "/song/getRankSongs").permitAll()
                                .requestMatchers("/jy_upload/**", "/jy_music/**", "/song/download/**").permitAll()

//                        // ② 按HTTP方法限制匿名访问
//                        .requestMatchers(HttpMethod.GET, "/api/notice", "/api/announcement").permitAll()
//
//                        // ③ 特定IP段可访问（Spring Security 6.x 写法）
//                        .requestMatchers("/admin/**")
//                        .access(IpAddressAuthorizationManager.hasIpAddress("192.168.1.0/24"))
//
//                        // ④ 需指定权限
//                        .requestMatchers("/sys/user/add").hasAuthority("sys:user:add")

                                // 兜底规则
                                .anyRequest().authenticated()
                )
//                禁用默认登录表单
//                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        // 自定义登出接口地址（默认是/logout，可修改）
                        .logoutUrl("/user/logout")
                        // 登出成功后的处理器（JWT场景需手动失效Token、清理缓存，返回JSON）
                        .logoutSuccessHandler(logoutSuccessHandler)
                        // 可选：登出时是否销毁HttpSession（无状态场景设为true，不影响）
                        .invalidateHttpSession(true)
                        // 可选：登出时是否清除认证信息
                        .clearAuthentication(true)
                        // 可选：登出时删除指定Cookie（如前端存储的Token Cookie）
                        // .deleteCookies("JWT-TOKEN", "SESSION-ID")
                        // 禁用默认的登出页面（前后端分离必配）
                        .permitAll()
                )
                // 添加JWT filter
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加CORS filter
                .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
                .addFilterBefore(corsFilter, LogoutFilter.class)
                .build();
    }
}
