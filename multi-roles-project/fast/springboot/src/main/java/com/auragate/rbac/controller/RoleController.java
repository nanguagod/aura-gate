package com.auragate.rbac.controller;

import com.auragate.rbac.domain.AjaxResult;
import com.auragate.rbac.domain.Role;
import com.auragate.rbac.domain.TableDataInfo;
import com.auragate.rbac.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色信息
 */
@RestController
@RequestMapping("/system/role")
public class RoleController extends BaseController{
    @Resource
    private IRoleService roleService;

    /**
     * 查询所有角色列表
     */
    @GetMapping("/selectAllRole")
    public AjaxResult selectAllRole() {
        return success(roleService.selectRoleList(new Role()));
    }

    /**
     * 查询角色列表
     */
    @GetMapping("/selectRoleList")
    public TableDataInfo selectRoleList(Role role) {
        startPage();
        List<Role> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }

    /**
     * 根据角色ID查询角色信息
     */
    @GetMapping("/selectRoleByRoleId/{roleId}")
    public AjaxResult selectRoleByRoleId(@PathVariable Long roleId) {
        return success(roleService.selectRoleByRoleId(roleId));
    }

    /**
     * 新增角色
     */
    @PostMapping("/insertRole")
    public AjaxResult insertRole(@RequestBody Role role) {
        return toAjax(roleService.insertRole(role));
    }

    /**
     * 修改角色
     */
    @PutMapping("/updateRole")
    public AjaxResult updateRole(@RequestBody Role role) {
        return toAjax(roleService.updateRole(role));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/deleteRoleByRoleIds/{roleIds}")
    public AjaxResult deleteRoleByRoleIds(@PathVariable Long[] roleIds) {
        for (Long roleId : roleIds) {
            if (roleId == 1L || roleId == 2L) {
                return error("超级管理员和普通用户是系统的核心角色, 不允许删除");
            }
        }
        return toAjax(roleService.deleteRoleByRoleIds(roleIds));
    }

}
