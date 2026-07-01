package com.auragate.rbac.controller;

import com.auragate.rbac.service.IUserService;
import com.auragate.rbac.service.IMenuService;
import com.auragate.common.constant.Constants;
import com.auragate.common.dto.AjaxResult;
import com.auragate.rbac.domain.User;
import com.auragate.rbac.domain.Menu;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
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

        // AI 对话数 — 优先读计数器，回退到 SCAN
        try {
            Object countObj = redisTemplate.opsForValue().get(Constants.AI_CONTEXT_COUNT_KEY);
            if (countObj != null) {
                stats.put("aiConversationCount", Integer.parseInt(countObj.toString()));
            } else {
                stats.put("aiConversationCount", scanCount(Constants.AI_CONTEXT_PREFIX + "*"));
            }
        } catch (Exception e) {
            log.warn("获取AI对话数失败", e);
            stats.put("aiConversationCount", 0);
        }

        // 任务结果数 — 优先读计数器，回退到 SCAN
        try {
            Object countObj = redisTemplate.opsForValue().get(Constants.TASK_RESULT_COUNT_KEY);
            if (countObj != null) {
                stats.put("knowledgeDocCount", Integer.parseInt(countObj.toString()));
            } else {
                stats.put("knowledgeDocCount", scanCount(Constants.TASK_RESULT_PREFIX + "*"));
            }
        } catch (Exception e) {
            log.warn("获取知识库文档数失败", e);
            stats.put("knowledgeDocCount", 0);
        }

        return success(stats);
    }

    /**
     * 使用 SCAN 命令计数（非阻塞，替代 KEYS）
     */
    private long scanCount(String pattern) {
        Set<String> keys = redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keySet = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().match(pattern).count(100).build())) {
                while (cursor.hasNext()) {
                    keySet.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                log.warn("SCAN 计数异常: pattern={}", pattern, e);
            }
            return keySet;
        });
        return keys != null ? keys.size() : 0;
    }
}
