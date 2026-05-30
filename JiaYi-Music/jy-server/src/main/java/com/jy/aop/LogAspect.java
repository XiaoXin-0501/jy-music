package com.jy.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LogAspect {
    @Pointcut("execution(* com.jy.controller..*.*(..))")
    public void controllerLog() {
    }

    @Pointcut("execution(* com.jy.service..*.*(..))" + "&& !execution(* com.jy.service.webSocketService..*.*(..))")
    public void serviceLog() {
    }

    // 前置通知：方法执行前
    @Before("controllerLog()")
    public void doBefore(JoinPoint joinPoint) {
        // 获取请求信息
        HttpServletRequest request = getRequest();
        if (request == null) return;
        log.info("[{}]请求开始", Thread.currentThread().getName());
        log.info("请求地址: {}", request.getRequestURL());
        log.info("请求方式: {}", request.getMethod());
        log.info("方法全路径: {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
        log.info("请求参数: {}", Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "controllerLog()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("[{}]请求返回", Thread.currentThread().getName());
        log.info("方法: {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
        log.info("返回结果: {}", result);
    }

    @AfterThrowing(pointcut = "serviceLog()", throwing = "ex")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 1. 方法信息
        HttpServletRequest request = getRequest();
        // ============== 异常定位日志 ==============
        StackTraceElement firstStack = ex.getStackTrace()[0];
        log.error("[{}]系统异常", Thread.currentThread().getName());
        log.error("异常类型: {}", ex.getClass().getName());
        log.error("异常信息: {}", ex.getMessage());
        log.error("报错位置: {}.{}({}:{})",
                firstStack.getClassName(),
                firstStack.getMethodName(),
                firstStack.getFileName(),
                firstStack.getLineNumber());
        if (request != null) {
            log.error("请求URL: {}", request.getRequestURL());
            log.error("请求IP: {}", request.getRemoteAddr());
            log.error("请求Method: {}", request.getMethod());
        }

    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}
