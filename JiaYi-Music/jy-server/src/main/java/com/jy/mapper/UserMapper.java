package com.jy.mapper;

import com.jy.entity.UserEntity;
import com.jy.vo.requestVo.UserInfoBody;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface UserMapper {
    UserEntity getUserByUserAccount(String userAccount);

    void updateLoginInfo(@Param("id") Long id, @Param("loginIp") String loginIp, @Param("loginTime") LocalDateTime loginTime);

    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    int updateUserInfo(@Param("id") Long id, @Param("userInfoBody") UserInfoBody userInfoBody);
}
