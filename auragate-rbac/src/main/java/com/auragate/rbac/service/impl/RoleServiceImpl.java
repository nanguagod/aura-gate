package com.auragate.rbac.service.impl;

import com.auragate.rbac.domain.Role;
import com.auragate.rbac.domain.RoleMenu;
import com.auragate.rbac.mapper.RoleMapper;
import com.auragate.rbac.mapper.RoleMenuMapper;
import com.auragate.rbac.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色 业务处理类
 */
@Service
public class RoleServiceImpl implements IRoleService {
    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    /**
     * 查询角色列表
     * @return 角色列表数据
     */
    @Override
    public List<Role> selectRoleList(Role role) {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 根据角色ID查询角色信息
     * @param roleId 角色ID
     * @return 角色信息
     */
    @Override
    public Role selectRoleByRoleId(Long roleId) {
        return roleMapper.selectRoleByRoleId(roleId);
    }

    /**
     * 新增角色
     * @param role 表单参数
     * @return 是否新增成功
     */
    @Override
    @Transactional
    public int insertRole(Role role) {
        //新增角色
        roleMapper.insertRole(role);
        //新增角色菜单关联信息
        return insetRoleMenu(role);
    }

    /**
     * 修改角色
     * @param role 表单参数
     * @return 是否修改成功
     */
    @Override
    @Transactional
    public int updateRole(Role role) {
        //修改角色
        roleMapper.updateRole(role);
        //根据角色ID删除角色和菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        //新增角色菜单信息
        return insetRoleMenu(role);
    }

    /**
     * 新增角色菜单信息
     */
    public int insetRoleMenu(Role role) {
        int rows = 1;
        // 空菜单列表守卫
        if (role.getMenuIds() == null || role.getMenuIds().length == 0) {
            return 0;
        }
        //新增用户与角色关联的数据
        ArrayList<RoleMenu> list = new ArrayList<>();
        for (Long menuId : role.getMenuIds()) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0) {
            //批量新增角色菜单关联
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 删除角色
     * @param roleIds 角色ID数组
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public int deleteRoleByRoleIds(Long[] roleIds) {
        //批量删除角色菜单关联信息
        roleMenuMapper.deleteRomeMenuByRoleIds(roleIds);
        return roleMapper.deleteRoleByRoleIds(roleIds);
    }
}
