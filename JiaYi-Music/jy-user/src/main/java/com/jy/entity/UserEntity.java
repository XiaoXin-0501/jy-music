package com.jy.entity;

import com.jy.vo.requestVo.UserInfoBody;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像
     */

    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别
     * 0 = 女
     * 1 = 男
     * 2 = 未知
     */
    private String sex;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 状态
     * 0 = 正常
     * 1 = 停用
     */
    private String status;

    /**
     * 最后登录 IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 角色ID数组
     */
    private List<RoleEntity> roles;

    public void setUserInfo(UserInfoBody userInfoBody) {
        this.nickName = userInfoBody.getNickName();
        this.sex = userInfoBody.getSex();
        this.email = userInfoBody.getEmail();
        this.birthday = userInfoBody.getBirthday();
    }
}
