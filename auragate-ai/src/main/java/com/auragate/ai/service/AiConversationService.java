package com.auragate.ai.service;

import com.auragate.common.constant.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 对话上下文服务 — 使用 Redis 存储对话历史
 */
@Slf4j
@Service
public class AiConversationService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存用户消息
     */
    public void saveUserMessage(Long userId, String message) {
        String key = Constants.AI_CONTEXT_PREFIX + userId;
        // 使用 List 存储消息，左推
        redisTemplate.opsForList().leftPush(key, "user: " + message);
        redisTemplate.expire(key, Constants.AI_CONTEXT_EXPIRE, TimeUnit.SECONDS);
        // 限制最多 50 条上下文
        Long size = redisTemplate.opsForList().size(key);
        if (size != null && size > 50) {
            redisTemplate.opsForList().trim(key, 0, 49);
        }
    }

    /**
     * 保存 AI 消息
     */
    public void saveAiMessage(Long userId, String message) {
        String key = Constants.AI_CONTEXT_PREFIX + userId;
        redisTemplate.opsForList().leftPush(key, "assistant: " + message);
        redisTemplate.expire(key, Constants.AI_CONTEXT_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 获取对话上下文（最近 N 条）
     */
    public List<String> getContext(Long userId, int count) {
        String key = Constants.AI_CONTEXT_PREFIX + userId;
        List<Object> range = redisTemplate.opsForList().range(key, 0, count - 1);
        if (range == null) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (Object o : range) {
            result.add(o.toString());
        }
        return result;
    }

    /**
     * 清除对话上下文
     */
    public void clearContext(Long userId) {
        String key = Constants.AI_CONTEXT_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
