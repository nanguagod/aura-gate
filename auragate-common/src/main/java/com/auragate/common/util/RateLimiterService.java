package com.auragate.common.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的简单 IP 限流服务
 * 使用 INCR + EXPIRE 实现滑动窗口计数器
 */
@Component
public class RateLimiterService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 检查是否允许请求。
     *
     * @param key      限流标识（如 login:192.168.1.1）
     * @param limit    时间窗口内允许的最大请求数
     * @param windowMs 时间窗口（毫秒）
     * @return true 允许; false 超出限流
     */
    public boolean tryAcquire(String key, int limit, long windowMs) {
        String redisKey = "rate_limit:" + key;
        Long count = stringRedisTemplate.opsForValue().increment(redisKey);
        if (count == null) {
            return true;
        }
        // 首次访问时设置过期时间
        if (count == 1) {
            stringRedisTemplate.expire(redisKey, windowMs, TimeUnit.MILLISECONDS);
        }
        return count <= limit;
    }

    /**
     * 获取客户端真实 IP
     */
    public static String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
