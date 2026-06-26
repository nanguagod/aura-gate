package com.auragate.rbac.mapper;

import com.auragate.rbac.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户 Mapper
 */
//@Mapper注解的作用:
//1. 告诉mybatis这是一个Mapper接口
//2. mybatis会自动为这个接口生成代理实现类
//3.SpringBoor启动时会扫描@Mapper注解的接口, 并且注册为Bean
@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     */
    public User selectUserByUserName(String userName);

    /**
     * 通过用户ID查询用户信息
     * @param userId 用户ID
     * @return 用户对象信息
     */
    User selectUserByUserId(Long userId);

    /**
     * 新增用户
     * @param user 用户信息
     * @return 是否新增成功
     */
    int insertUser(User user);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatar 头像访问路径
     * @return 是否更新成功
     */
    int updateUserAvatar(Long userId, String avatar);

    /**
     * 修改用户信息
     * @param user 用户信息
     * @return 是否修改成功
     */
    int updateUser(User user);

    /**
     * 重置密码
     * @param userId 用户ID
     * @param newPassword 要修改成的密码
     * @return 是否重置成功
     */
    int resetUserPwd(Long userId, String newPassword);

    /**
     * 查询用户列表
     * @param user 查询参数
     * @return 用户列表数据
     */
    List<User> selectUserList(User user);

    /**
     * 删除用户
     * @param userIds 用户ID数组
     * @return 是否删除成功
     */
    int deleteUserByUserIds(Long[] userIds);
}
