package com.jy.domain;

import com.jy.entity.RoleEntity;
import com.jy.entity.UserEntity;
import lombok.Data;
//import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SecurityUser implements UserDetails {
    private static final long serialVersionUID = 1L;

    private UserEntity userEntity;

    /**
     * 用户唯一标识
     */
    private String token;

    private String csrfToken;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    public SecurityUser(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //空值防护：避免userEntity/roles为null导致空指针
        if (userEntity == null || userEntity.getRoles() == null || userEntity.getRoles().isEmpty()) {
            return List.of(); // 返回空不可变列表，代表无任何角色权限
        }

        //转换角色为Spring Security标准权限对象
        List<RoleEntity> roles = userEntity.getRoles();
        return roles.stream()
                // 过滤无效角色：排除roleName为null/空字符串的情况（数据防护）
                .filter(role -> role.getRoleName() != null && !role.getRoleName().trim().isEmpty())
                // 核心：拼接ROLE_前缀 + 转换为SimpleGrantedAuthority（GrantedAuthority默认实现类）
                // 原因：Spring Security的hasRole()注解会自动拼接ROLE_，无前缀会导致角色校验失效
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName().trim()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getAccount();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "0".equals(userEntity.getStatus());
    }
}
