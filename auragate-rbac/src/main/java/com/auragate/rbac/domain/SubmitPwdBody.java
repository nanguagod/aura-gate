package com.auragate.rbac.domain;

import lombok.Data;

/**
 * 修改个人密码时的对象
 */
@Data
public class SubmitPwdBody {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
