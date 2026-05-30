package com.jy.service;

import com.jy.entity.UserEntity;
import com.jy.vo.requestVo.UserInfoBody;
import com.jy.vo.responseVo.UserInformation;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public interface UserService {
    /**
     * 通过账号（username）获取信息
     *
     * @param userAccount
     * @return
     */
    public UserEntity getUserByUserAccount(String userAccount);

    /**
     * 更新用户登录信息（IP和登录时间）
     *
     * @param id        用户ID
     * @param loginIp   登录IP地址
     * @param loginTime 登录时间
     * @return 结果
     */
    public void updateLoginInfo(Long id, String loginIp, LocalDateTime loginTime);

    /**
     * 获取用户信息
     *
     * @return
     */
    public UserInformation getUserInfo();

    public String updateAvatar(Long id, MultipartFile file);

    public boolean updateUserInfo(Long id, UserInfoBody userInfoBody);

    boolean logout(Long id);
}
