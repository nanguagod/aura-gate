package com.auragate.rbac.mapper;

import com.auragate.rbac.domain.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

/**
 * 角色与菜单关联 数据层
 */
@Mapper
public interface RoleMenuMapper {

    /**
     * 根据角色ID删除角色和菜单关联
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    int deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量新增角色菜单关联
     * @param roleMenuList 角色菜单列表
     * @return 是否新增成功
     */
    int batchRoleMenu(ArrayList<RoleMenu> roleMenuList);

    /**
     * 批量删除角色菜单关联信息
     * @param roleIds 要删除的单个或多个角色ID
     * @return 是否删除成功
     */
    int deleteRomeMenuByRoleIds(Long[] roleIds);
}
