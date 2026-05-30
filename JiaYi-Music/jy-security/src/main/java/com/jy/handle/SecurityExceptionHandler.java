package com.jy.handle;

import com.jy.constant.ErrorMessage;
import com.jy.exception.UserException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

//转发到全局统一处理
@Slf4j
@Component
public class SecurityExceptionHandler implements AccessDeniedHandler, AuthenticationEntryPoint {
    private final HandlerExceptionResolver resolver;

    public SecurityExceptionHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.info(authException.getMessage());
        resolver.resolveException(
                request,
                response,
                null,
                new UserException(ErrorMessage.getByMessage(authException.getMessage()))
        );
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info(accessDeniedException.getMessage());
        resolver.resolveException(
                request,
                response,
                null,
                new UserException(ErrorMessage.getByMessage(accessDeniedException.getMessage()))
        );
    }
}
