package com.jy.service.userService;

import com.jy.cache.RedisService;
import com.jy.constant.RedisKey;
import com.jy.domain.SecurityUser;
import com.jy.entity.UserEntity;
import com.jy.mapper.UserMapper;
import com.jy.securityUtils.SecurityUtils;
import com.jy.service.TokenService;
import com.jy.service.UserService;
import com.jy.service.commonService.IOService;
import com.jy.vo.requestVo.UserInfoBody;
import com.jy.vo.responseVo.UserInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Component
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final IOService ioService;
    private final String avatarPath;
    private final TokenService tokenService;
    private final RedisService redisService;

    UserServiceImpl(UserMapper userMapper,
                    IOService ioService,
                    @Value("${file.user.path.avatar}") String avatarPath,
                    TokenService tokenService,
                    RedisService redisService
    ) {
        this.userMapper = userMapper;
        this.ioService = ioService;
        this.avatarPath = avatarPath;
        this.tokenService = tokenService;
        this.redisService = redisService;
    }

    @Override
    public UserEntity getUserByUserAccount(String userAccount) {
        return userMapper.getUserByUserAccount(userAccount);
    }

    /**
     * 更新用户登录信息（IP和登录时间）
     *
     * @param id        用户ID
     * @param loginIp   登录IP地址
     * @param loginTime 登录时间
     * @return 结果
     */
    public void updateLoginInfo(Long id, String loginIp, LocalDateTime loginTime) {
        userMapper.updateLoginInfo(id, loginIp, loginTime);
    }

    @Override
    public UserInformation getUserInfo() {
        UserEntity entity = SecurityUtils.getUserEntity();
        return new UserInformation(
                entity.getId(),
                entity.getNickName(),
                ioService.getUrl(entity.getAvatar()),
                entity.getEmail(),
                entity.getAccount(),
                entity.getSex(),
                entity.getStatus(),
                entity.getBirthday(),
                entity.getRoles()
        );
    }

    @Override
    public String updateAvatar(Long id, MultipartFile file) {
        String localPath = ioService.uploadToLocal(file, avatarPath);
        String relPath = ioService.getRelPath(localPath);
        userMapper.updateAvatar(id, relPath);
        SecurityUser securityUser = SecurityUtils.getSecurityUser();
        securityUser.getUserEntity().setAvatar(relPath);
        tokenService.setToken(securityUser);
        return ioService.getUrl(relPath);
    }

    @Override
    public boolean updateUserInfo(Long id, UserInfoBody userInfoBody) {
        int res = userMapper.updateUserInfo(id, userInfoBody);
        if (res <= 0) return false;
        SecurityUser securityUser = SecurityUtils.getSecurityUser();
        securityUser.getUserEntity().setUserInfo(userInfoBody);
        tokenService.setToken(securityUser);
        return true;
    }

    @Override
    public boolean logout(Long id) {
        return redisService.delete(
                RedisKey.USER_TOKEN.getKey(id.toString()),
                RedisKey.USER_CSRF_TOKEN.getKey(id.toString())
        );
    }
}
