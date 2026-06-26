package com.auragate.rbac.mapper;

import com.auragate.rbac.domain.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper {

    /**
     * 查询角色列表
     * @return 角色列表数据
     */
    List<Role> selectRoleList(Role role);

    /**
     * 根据角色ID查询角色信息
     * @param roleId 角色ID
     * @return 角色信息
     */
    Role selectRoleByRoleId(Long roleId);

    /**
     * 新增角色
     * @param role 表单参数
     * @return 是否新增成功
     */
    int insertRole(Role role);

    /**
     * 修改角色
     * @param role 表单参数
     * @return 是否修改成功
     */
    int updateRole(Role role);

    /**
     * 删除角色
     * @param roleIds 角色ID数组
     * @return 是否删除成功
     */
    int deleteRoleByRoleIds(Long[] roleIds);
}
