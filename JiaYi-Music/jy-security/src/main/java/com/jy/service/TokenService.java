package com.jy.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jy.cache.RedisService;
import com.jy.constant.CookieConstants;
import com.jy.constant.ErrorMessage;
import com.jy.constant.RedisKey;
import com.jy.constant.UserConstants;
import com.jy.domain.SecurityUser;
import com.jy.utils.CookieUtils;
import com.jy.utils.IdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


//暂未加入csrf的完全防御
@Component
public class TokenService {
    private final String header;
    private final String prefix;
    private final String secret;
    private final int expireTime;
    private final int refreshExpireTime;
    private final RedisService redisService;

    public TokenService(@Value("${security.token.header}") String header,
                        @Value("${security.token.prefix}") String prefix,
                        @Value("${security.token.secret}") String secret,
                        @Value("${security.token.expireTime}") int expireTime,
                        @Value("${security.token.refreshExpireTime}") int refreshExpireTime,
                        RedisService redisService
    ) {
        this.header = header;
        this.prefix = prefix;
        this.secret = secret;
        this.expireTime = expireTime;
        this.refreshExpireTime = refreshExpireTime;
        this.redisService = redisService;
    }

    /**
     * 创建令牌
     *
     * @param securityUser 用户信息
     * @return 令牌
     */
    public Map<String, String> createToken(SecurityUser securityUser) {
        //删除旧的用户信息,如果用户手动清除缓存就可能出现冗余缓存
        String user_id = securityUser.getUserEntity().getId().toString();
        String oldToken = redisService.get(RedisKey.USER_TOKEN.getKey(user_id), String.class);
        String user_old_login = RedisKey.USER_LOGIN.getKey(oldToken);
        redisService.delete(user_old_login);

        String token = IdUtils.uuid();
        String csrfToken = IdUtils.uuid();
        securityUser.setToken(token);
        securityUser.setCsrfToken(csrfToken);
        setToken(securityUser);

        token = JWT.create()
                .withClaim(RedisKey.USER_TOKEN.getKey(), token)
                .withClaim(UserConstants.USER_ID, securityUser.getUserEntity().getId().toString())
                .sign(Algorithm.HMAC512(secret));
        csrfToken = JWT.create()
                .withClaim(RedisKey.USER_CSRF_TOKEN.getKey(), csrfToken)
                .withClaim(UserConstants.USER_ID, securityUser.getUserEntity().getId().toString())
                .sign(Algorithm.HMAC512(secret));
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("token", token);
        resultMap.put("csrfToken", csrfToken);
        return resultMap;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param jwt 令牌
     * @return 数据声明
     */
    private DecodedJWT parseToken(String jwt) {
        try {
            return JWT.require(Algorithm.HMAC512(secret)).build().verify(jwt);
        } catch (Exception e) {
            throw new BadCredentialsException(ErrorMessage.USER_TOKEN_ERROR.getMessage());
        }
    }

    public SecurityUser getSecurityUser(HttpServletRequest request) {
        String jwt = CookieUtils.get(request, CookieConstants.TOKEN);
        String head = request.getHeader(header);
        if (head == null) return null;
        String csrfJwt = head.replace(prefix, "");
        if (jwt != null && !csrfJwt.isEmpty()) {
            DecodedJWT tokenDJwt = parseToken(jwt);
            DecodedJWT csrfDJwt = parseToken(csrfJwt);
            String userId = tokenDJwt.getClaim(UserConstants.USER_ID).asString();
            String token = tokenDJwt.getClaim(RedisKey.USER_TOKEN.getKey()).asString();
            String csrfToken = csrfDJwt.getClaim(RedisKey.USER_CSRF_TOKEN.getKey()).asString();
            String realCsrfToken = redisService.get(RedisKey.USER_CSRF_TOKEN.getKey(userId), String.class);
            String realToken = redisService.get(RedisKey.USER_TOKEN.getKey(userId), String.class);
            SecurityUser securityUser = redisService.get(RedisKey.USER_LOGIN.getKey(token), SecurityUser.class);
            if (realCsrfToken == null || realToken == null || securityUser == null) {
                throw new BadCredentialsException(ErrorMessage.USER_TOKEN_EXPIRE.getMessage());
            }
            if (!csrfToken.equals(realCsrfToken) || !token.equals(realToken)) {
                throw new BadCredentialsException(ErrorMessage.USER_TOKEN_ERROR.getMessage());
            }
            return securityUser;
        }
        return null;
    }

    public void setToken(SecurityUser securityUser) {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        securityUser.setLoginTime(now);

        // 过期时间
        LocalDateTime expire = now.plusMinutes(expireTime);
        securityUser.setExpireTime(expire);

        //获取存储token和存储securityUser的key
        String token = securityUser.getToken();
        String csrfToken = securityUser.getCsrfToken();
        String user_id = securityUser.getUserEntity().getId().toString();

        String user_token = RedisKey.USER_TOKEN.getKey(user_id);
        String user_csrf_token = RedisKey.USER_CSRF_TOKEN.getKey(user_id);
        String user_login = RedisKey.USER_LOGIN.getKey(token);

        redisService.set(user_token, token);
        redisService.set(user_login, securityUser);
        redisService.set(user_csrf_token, csrfToken);

        //设置过期时间
        redisService.expire(user_token, expireTime, TimeUnit.MINUTES);
        redisService.expire(user_login, expireTime, TimeUnit.MINUTES);
        redisService.expire(user_csrf_token, expireTime, TimeUnit.MINUTES);
    }

    public void delSecurityUser(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtils.get(request, CookieConstants.TOKEN);
        if (token == null) {
            return;
        }
        DecodedJWT DJwt = JWT.decode(token);
        String user_id = DJwt.getClaim(UserConstants.USER_ID).asString();
        String user_token = RedisKey.USER_TOKEN.getKey(user_id);
        String oldToken = redisService.get(RedisKey.USER_TOKEN.getKey(user_id), String.class);
        String user_old_login = RedisKey.USER_LOGIN.getKey(oldToken);
        String user_csrf_token = RedisKey.USER_CSRF_TOKEN.getKey(user_id);
        CookieUtils.delete(response, CookieConstants.TOKEN);
        redisService.delete(user_old_login, user_token, user_csrf_token);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param securityUser 登录信息
     */
    public void refreshToken(SecurityUser securityUser) {
        LocalDateTime expireTime = securityUser.getExpireTime();
        LocalDateTime now = LocalDateTime.now();
        // 计算剩余分钟
        long remainMinutes = ChronoUnit.MINUTES.between(now, expireTime);
        // 剩余时间 <= 刷新时间 → 自动刷新
        if (remainMinutes <= 0) {
            throw new BadCredentialsException(ErrorMessage.USER_TOKEN_EXPIRE.getMessage());
        } else if (remainMinutes <= refreshExpireTime) {
            setToken(securityUser);
        }
    }
}
