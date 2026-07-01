package com.auragate.rbac.controller;

import com.auragate.common.util.RateLimiterService;
import com.auragate.common.dto.AjaxResult;
import com.auragate.rbac.domain.RegisterBody;
import com.auragate.rbac.domain.User;
import com.auragate.rbac.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户注册
 */
@RestController
public class RegisterController extends BaseController{
    @Resource
    private IUserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private RateLimiterService rateLimiterService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody registerBody, HttpServletRequest request) {
        // IP 限流 — 每 IP 每分钟最多 3 次注册
        String clientIp = RateLimiterService.getClientIp(request);
        if (!rateLimiterService.tryAcquire("register:" + clientIp, 3, 60_000)) {
            return error("请求过于频繁，请稍后重试");
        }

        String userName = registerBody.getUserName();
        String password = registerBody.getPassword();

        //验证输入参数
        if (userName == null || userName.isEmpty()) {
            return error("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return error("密码不能为空");
        }

        //创建用户对象
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setPassword(passwordEncoder.encode(password));

        //执行注册逻辑
        boolean regFlag = userService.registerUser(newUser);
        if (!regFlag) {
            return error("注册失败");
        }

        return success();
    }

}
