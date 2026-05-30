package com.jy.filter;

import com.jy.constant.CookieConstants;
import com.jy.domain.SecurityUser;
import com.jy.handle.SecurityExceptionHandler;
import com.jy.service.TokenService;
import com.jy.utils.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * token过滤器 验证token有效性
 *
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final SecurityExceptionHandler securityExceptionHandler;

    public JwtAuthenticationTokenFilter(TokenService tokenService, SecurityExceptionHandler securityExceptionHandler) {
        this.tokenService = tokenService;
        this.securityExceptionHandler = securityExceptionHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.contains("/captcha") || uri.contains("/login") || uri.contains("/jy_upload") || uri.contains("ws")) {
            chain.doFilter(request, response);
            return;
        }
        SecurityUser securityUser = null;
        try {
            securityUser = tokenService.getSecurityUser(request);
            if (securityUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                tokenService.refreshToken(securityUser);
                //构造认证对象，表示认证通过
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
                //设置认证详情信息
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //将认证信息设置到上下文
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (AuthenticationException e) {
            tokenService.delSecurityUser(request, response);
            SecurityContextHolder.clearContext();
            securityExceptionHandler.commence(request, response, e);
            return;
        }
        chain.doFilter(request, response);
    }
}