package com.auragate.rbac.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 角色对象 role
 */
@Data
public class Role {
    //角色ID
    private Long roleId;
    //角色名称
    private String roleName;
    //权限标识
    private String roleKey;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    //菜单组
    private Long[] menuIds;
}
