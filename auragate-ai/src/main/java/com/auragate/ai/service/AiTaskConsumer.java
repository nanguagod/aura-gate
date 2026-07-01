package com.auragate.ai.service;

import com.auragate.ai.agent.AuraAgent;
import com.auragate.common.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI 异步任务消费者 — 接收 RabbitMQ 消息，调用 AuraAgent 处理并写入 Redis
 */
@Slf4j
@Component
@RabbitListener(queues = "${spring.rabbitmq.ai.queue:" + Constants.AI_TASK_QUEUE + "}")
public class AiTaskConsumer {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectProvider<AuraAgent> auraAgentProvider;

    @RabbitHandler
    public void handleAiTask(Map<String, Object> task) {
        try {
            String taskId = (String) task.get("taskId");
            Long userId = Long.valueOf(task.get("userId").toString());
            String message = (String) task.get("message");

            log.info("处理 AI 任务: taskId={}, userId={}, message={}", taskId, userId, message);

            // 调用 AuraAgent 处理
            AuraAgent agent = auraAgentProvider.getObject();
            String result = agent.run(message);

            // 将结果写入 Redis
            String resultKey = Constants.TASK_RESULT_PREFIX + taskId;
            redisTemplate.opsForValue().increment(Constants.TASK_RESULT_COUNT_KEY);
            redisTemplate.opsForValue().set(resultKey, result, Constants.TASK_RESULT_EXPIRE, java.util.concurrent.TimeUnit.SECONDS);

            log.info("AI 任务完成: taskId={}, resultLength={}", taskId, result != null ? result.length() : 0);
        } catch (Exception e) {
            log.error("处理 AI 任务失败", e);
            // 将错误信息写入 Redis 供客户端查询
            try {
                String taskId = (String) task.get("taskId");
                String resultKey = Constants.TASK_RESULT_PREFIX + taskId;
                redisTemplate.opsForValue().increment(Constants.TASK_RESULT_COUNT_KEY);
                redisTemplate.opsForValue().set(resultKey, "AI 处理失败: " + e.getMessage(),
                        Constants.TASK_RESULT_EXPIRE, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.error("写入错误结果到 Redis 失败", ex);
            }
        }
    }
}
