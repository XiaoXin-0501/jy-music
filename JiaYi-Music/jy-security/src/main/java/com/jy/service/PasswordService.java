package com.jy.service;

import com.jy.cache.RedisService;
import com.jy.constant.ErrorMessage;
import com.jy.constant.RedisKey;
import com.jy.context.AuthenticationContextHolder;
import com.jy.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PasswordService {
    private final int maxRetryCount;
    private final int lockTime;
    private final RedisService redisService;

    public PasswordService(@Value(value = "${security.user.password.maxRetryCount}") int maxRetryCount,
                           @Value(value = "${security.user.password.lockTime}") int lockTime,
                           RedisService redisService
    ) {
        this.maxRetryCount = maxRetryCount;
        this.lockTime = lockTime;
        this.redisService = redisService;
    }

    private boolean matches(UserEntity user, String rawPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        System.out.println("123456对应的BCrypt密文：" + passwordEncoder.encode("123456"));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void validate(UserEntity user) {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();
        String pwd_error_key = RedisKey.USER_PWD_ERR.getKey(user.getId().toString());
        Integer retryCount = redisService.get(pwd_error_key, Integer.class);
        if (retryCount == null) {
            retryCount = 0;
        }
        if (retryCount >= maxRetryCount) {
            throw new BadCredentialsException(ErrorMessage.USER_PASSWORD_RETRY_LIMIT_EXCEED.getMessage());
        }
        if (!matches(user, password)) {
            retryCount++;
            redisService.set(pwd_error_key, retryCount);
            redisService.expire(pwd_error_key, lockTime, TimeUnit.MINUTES);
            throw new BadCredentialsException(ErrorMessage.USER_PASSWORD_NOT_MATCH.getMessage());
        } else {
            redisService.delete(pwd_error_key);
        }
    }
}
