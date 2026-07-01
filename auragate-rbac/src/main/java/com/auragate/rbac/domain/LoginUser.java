package com.auragate.rbac.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户身份类
 * 作用: 封装登录用户的信息,
 * 相当于系统内部的"用户身份证"
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {
    //用户ID:唯一标识
    private Long userId;

    //登录时间: 用户登录的时间戳 (毫秒)
    private Long loginTime;

    //过期时间: token的过期时间戳
    private Long expireTime;

    //用户信息: 关联的User实体信息
    private User user;

    //用户权限标识集合（如 system:user:list）
    private Set<String> permissions = new HashSet<>();

    //带参数的构造方法
    public LoginUser(Long userId, User user) {
        this.userId = userId;
        this.user = user;
    }

    /**
     * 获取用户的权限集合
     * @return 权限列表（不为 null）
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的密码
     * @return 用户的密码
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 获取用户的用户名
     * @return 用户的用户名
     */
    @Override
    public String getUsername() {
        return user.getUserName();
    }
}
