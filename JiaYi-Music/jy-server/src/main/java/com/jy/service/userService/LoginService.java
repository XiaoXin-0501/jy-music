package com.jy.service.userService;

import com.jy.context.AuthenticationContextHolder;
import com.jy.domain.SecurityUser;
import com.jy.service.CaptchaService;
import com.jy.service.TokenService;
import com.jy.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class LoginService {
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public Map<String, String> login(String username, String password, String code, String uuid) {
        // 验证码校验
        captchaService.validateCaptcha(code, uuid);
        // 登录前置校验
//        loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } finally {
            AuthenticationContextHolder.clearContext();
        }
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        recordLoginInfo(securityUser.getUserEntity().getId());
        // 生成token
        return tokenService.createToken(securityUser);
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        userService.updateLoginInfo(userId, "IpUtils.getIpAddr()", LocalDateTime.now());
    }
}
