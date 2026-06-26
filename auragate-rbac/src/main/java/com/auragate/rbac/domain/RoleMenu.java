package com.auragate.rbac.domain;

import lombok.Data;

/**
 * 角色菜单关联 role_menu
 */
@Data
public class RoleMenu {
    //角色ID
    private Long roleId;
    //菜单ID
    private Long menuId;
}
