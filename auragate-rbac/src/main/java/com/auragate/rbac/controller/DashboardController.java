package com.auragate.rbac.controller;

import com.auragate.rbac.service.IUserService;
import com.auragate.rbac.service.IMenuService;
import com.auragate.rbac.domain.AjaxResult;
import com.auragate.rbac.domain.User;
import com.auragate.rbac.domain.Menu;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Dashboard 统计数据控制器 — 为首页提供动态数据
 */
@Slf4j
@RestController
public class DashboardController extends BaseController {

    @Resource
    private IUserService userService;

    @Resource
    private IMenuService menuService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Dashboard 统计数据
     */
    @GetMapping("/dashboard/stats")
    public AjaxResult stats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // 用户数
        try {
            List<User> users = userService.selectUserList(new User());
            stats.put("userCount", users != null ? users.size() : 0);
        } catch (Exception e) {
            log.warn("获取用户数失败", e);
            stats.put("userCount", 0);
        }

        // 菜单数
        try {
            List<Menu> menus = menuService.selectMenuList(new Menu(), null);
            stats.put("menuCount", menus != null ? menus.size() : 0);
        } catch (Exception e) {
            log.warn("获取菜单数失败", e);
            stats.put("menuCount", 0);
        }

        // AI 对话数（Redis ai:context 前缀的 key 数量）
        try {
            Set<String> aiContextKeys = redisTemplate.keys("auragate:ai:context:*");
            stats.put("aiConversationCount", aiContextKeys != null ? aiContextKeys.size() : 0);
        } catch (Exception e) {
            log.warn("获取AI对话数失败", e);
            stats.put("aiConversationCount", 0);
        }

        // 知识库文档数（Redis ai:context 前缀 + Task 相关作为概览）
        try {
            Set<String> taskKeys = redisTemplate.keys("auragate:task:result:*");
            stats.put("knowledgeDocCount", taskKeys != null ? taskKeys.size() : 0);
        } catch (Exception e) {
            log.warn("获取知识库文档数失败", e);
            stats.put("knowledgeDocCount", 0);
        }

        return success(stats);
    }
}
