package com.auragate.ai.service;

import com.auragate.common.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI 异步任务消费者 — 处理 AI 任务并将结果写入 Redis
 */
@Slf4j
@Component
@RabbitListener(queues = "${spring.rabbitmq.ai.queue:" + Constants.AI_TASK_QUEUE + "}")
public class AiTaskConsumer {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @RabbitHandler
    public void handleAiTask(Map<String, Object> task) {
        try {
            String taskId = (String) task.get("taskId");
            Long userId = Long.valueOf(task.get("userId").toString());
            String message = (String) task.get("message");

            log.info("处理 AI 任务: taskId={}, message={}", taskId, message);

            // 模拟 AI 处理（后续可调用 AuraAgent）
            String result = "AI 已处理消息: " + message;

            // 将结果写入 Redis
            String resultKey = Constants.TASK_RESULT_PREFIX + taskId;
            redisTemplate.opsForValue().set(resultKey, result, Constants.TASK_RESULT_EXPIRE, java.util.concurrent.TimeUnit.SECONDS);

            log.info("AI 任务完成: taskId={}", taskId);
        } catch (Exception e) {
            log.error("处理 AI 任务失败", e);
        }
    }
}
