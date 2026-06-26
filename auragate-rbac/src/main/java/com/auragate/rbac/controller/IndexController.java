package com.auragate.rbac.controller;

import com.auragate.rbac.domain.AjaxResult;
import com.auragate.rbac.domain.User;
import com.auragate.rbac.service.IUserService;
import com.auragate.rbac.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器, 处理根路径请求
 */
@RestController //标记这个类是一个REST API控制器 (它会自动把返回值转换为json格式)
@RequestMapping("/")
public class IndexController extends BaseController{
    @Resource
    private IUserService userService;

    @GetMapping
    public AjaxResult home() {
        return success("恭喜你, 成功启动了后端~");
    }

    /**
     * 测试接口
     */
    @GetMapping("/text")
    public AjaxResult test() {
        return success(SecurityUtils.getUserId());
    }

}
