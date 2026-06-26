package com.auragate.rbac.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与角色关联 mapper
 */
@Mapper
public interface UserRoleMapper {

    /**
     * 新增用户和角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否新增成功
     */
    int insertUserRole(Long userId, Long roleId);

    /**
     * 根据用户ID删除用户和角色关联
     * @param userId 用户ID
     * @return 是否删除成功
     */
    int deleteUserRoleByUserId(Long userId);

    /**
     * 批量删除用户与角色关联
     * @param userIds 要删除的单个或多个用户ID
     * @return 是否删除成功
     */
    int deleteUserRoles(Long[] userIds);

    /**
     * 根据用户ID查询角色ID
     * @param userId 用户ID
     * @return 角色ID
     */
    Long selectRoleIdByUserId(Long userId);
}
