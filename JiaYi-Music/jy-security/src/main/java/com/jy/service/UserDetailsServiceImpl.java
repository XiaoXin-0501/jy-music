package com.jy.service;

import com.jy.constant.ErrorMessage;
import com.jy.constant.UserConstants;
import com.jy.domain.SecurityUser;
import com.jy.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userService.getUserByUserAccount(username);
        if (userEntity == null) {
            log.info("登录用户：{}不存在", username);
            // 抛出Spring Security内部异常（最好用BadCredentialsException，否则异常信息可能被吞掉），而非自定义异常,自定义异常无法被全局捕获
            throw new BadCredentialsException(ErrorMessage.USER_NOT_EXIST.getMessage());
        } else if (UserConstants.LOCKED.equals(userEntity.getStatus())) {
            throw new BadCredentialsException(ErrorMessage.USER_IS_LOCKED.getMessage());
        }
        passwordService.validate(userEntity);
        return new SecurityUser(userEntity);
    }
}
