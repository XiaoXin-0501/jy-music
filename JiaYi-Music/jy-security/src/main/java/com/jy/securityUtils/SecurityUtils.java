package com.jy.securityUtils;

import com.jy.constant.ErrorMessage;
import com.jy.domain.SecurityUser;
import com.jy.entity.UserEntity;
import com.jy.exception.UserException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static SecurityUser getSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UserException(ErrorMessage.USER_GET_INFO_FAIL);
        }
        return (SecurityUser) authentication.getPrincipal();
    }

    public static UserEntity getUserEntity() {
        return SecurityUtils.getSecurityUser().getUserEntity();
    }

    public static Long getUserId() {
        return SecurityUtils.getUserEntity().getId();
    }
}
