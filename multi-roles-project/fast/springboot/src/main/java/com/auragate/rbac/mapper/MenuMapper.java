package com.auragate.rbac.mapper;

import com.auragate.rbac.domain.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface MenuMapper {

    /**
     * 根据用户ID查询菜单列表
     * @param menu 查询参数
     * @return 菜单列表数据
     */
    List<Menu> selectMenuListByUserId(Menu menu);

    /**
     * 新增菜单
     * @param menu 表单参数
     * @return 是否新增成功
     */
    int insertMenu(Menu menu);

    /**
     * 根据菜单ID查询菜单信息
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    Menu selectMenuByMenuId(Long menuId);

    /**
     * 修改菜单
     * @param menu 表单参数
     * @return 是否修改成功
     */
    int updateMenu(Menu menu);

    /**
     * 删除菜单
     * @param menuId 菜单ID
     * @return 是否删除成功
     */
    int deleteMenuByMenuId(Long menuId);

    /**
     * 根据角色ID查询对应菜单树
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    List<Long> selectMenuListByRoleId(Long roleId);
}
