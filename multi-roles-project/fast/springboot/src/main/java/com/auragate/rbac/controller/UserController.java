package com.auragate.rbac.controller;

import com.auragate.rbac.domain.AjaxResult;
import com.auragate.rbac.domain.TableDataInfo;
import com.auragate.rbac.domain.User;
import com.auragate.rbac.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息
 */
@RestController
@RequestMapping("/system/user")
public class UserController extends BaseController{
    @Resource
    private IUserService userService;

    /**
     * 查询用户列表
     */
    @GetMapping("/selectUserList")
    public TableDataInfo selectUserList(User user) {
        //启动分页功能
        startPage();
        //查询用户列表
        List<User> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/selectUserByUserId/{userId}")
    public AjaxResult selectUserByUserId(@PathVariable Long userId) {
        User user = userService.selectUserByUserId(userId);
        return success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping("/insertUser")
    public AjaxResult insertUser(@RequestBody User user) {
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PutMapping("/updateUser")
    public AjaxResult updateUser(@RequestBody User user) {
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/deleteUserByUserIds/{userIds}")
    public AjaxResult deleteUserByUserIds(@PathVariable Long[] userIds) {
        return toAjax(userService.deleteUserByUserIds(userIds));
    }

}
