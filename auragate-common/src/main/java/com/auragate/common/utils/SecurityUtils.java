package com.auragate.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类
 */
public class SecurityUtils {

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static Object getLoginUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            return authentication.getPrincipal();
        }
        return null;
    }

    public static Long getUserId() {
        Object principal = getLoginUser();
        if (principal instanceof com.auragate.common.dto.LoginUser) {
            return ((com.auragate.common.dto.LoginUser) principal).getUserId();
        }
        return null;
    }
}
