package com.auragate.rbac.service.impl;

import com.auragate.rbac.constants.RoleIdConstants;
import com.auragate.rbac.domain.User;
import com.auragate.rbac.mapper.UserMapper;
import com.auragate.rbac.mapper.UserRoleMapper;
import com.auragate.rbac.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户 service实现方法
 */
//@Service 注解的作用
//1. 告诉spring这是一个服务层组件(bean)
//2.spring会自动创建这个类的实例并且管理它
@Service
public class UserServiceImpl implements IUserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     * 通过用户名查询用户
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public User selectUserByUserName(String userName) {
        return userMapper.selectUserByUserName(userName);
    }

    /**
     * 通过用户ID查询用户信息
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public User selectUserByUserId(Long userId) {
        return userMapper.selectUserByUserId(userId);
    }

    /**
     * 注册用户
     * @param newUser 要注册的用户信息
     * @return 是否注册成功
     */
    @Override
    public boolean registerUser(User newUser) {
        //根据用户名查询用户信息
        User user = userMapper.selectUserByUserName(newUser.getUserName());

        if (user != null) {
            throw new RuntimeException("用户名已存在, 请更换用户名后再注册");
        }

        int i = userMapper.insertUser(newUser);

        //注册用户后默认为普通用户
        userRoleMapper.insertUserRole(newUser.getUserId(), RoleIdConstants.USER_ROLE_ID);

        return i > 0;
    }

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatar 头像访问路径
     * @return 是否更新成功
     */
    @Override
    public int updateUserAvatar(Long userId, String avatar) {
        return userMapper.updateUserAvatar(userId, avatar);
    }

    /**
     * 修改用户信息
     * @param user 用户信息
     * @return 是否修改成功
     */
    @Override
    @Transactional
    public int updateUser(User user) {
        if (user.getRoleId() == null) {
            //修改用户信息
            return userMapper.updateUser(user);
        } else {
            //修改用户信息
            userMapper.updateUser(user);

            //先删除用户之前的角色关联 (根据用户ID删除用户和角色关联)
            userRoleMapper.deleteUserRoleByUserId(user.getUserId());

            //新增用户和角色关联
            return userRoleMapper.insertUserRole(user.getUserId(), user.getRoleId());
        }
    }

    /**
     * 重置密码
     * @param userId 用户ID
     * @param newPassword 要修改成的密码
     * @return 是否重置成功
     */
    @Override
    public int resetUserPwd(Long userId, String newPassword) {
        return userMapper.resetUserPwd(userId, newPassword);
    }

    /**
     * 查询用户列表
     * @param user 查询参数
     * @return 用户列表数据
     */
    @Override
    public List<User> selectUserList(User user) {
        return userMapper.selectUserList(user);
    }

    /**
     * 新增用户
     * @param user 表单参数
     * @return 是否新增成功
     */
    @Override
    @Transactional
    public int insertUser(User user) {
        //新增用户
        userMapper.insertUser(user);

        //新增用户和角色关联
        return userRoleMapper.insertUserRole(user.getUserId(), user.getRoleId());
    }

    /**
     * 删除用户
     * @param userIds 用户ID数组
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByUserIds(Long[] userIds) {
        //批量删除用户与角色关联
        userRoleMapper.deleteUserRoles(userIds);

        return userMapper.deleteUserByUserIds(userIds);
    }

}
