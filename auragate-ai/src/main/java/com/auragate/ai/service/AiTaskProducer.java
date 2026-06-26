package com.auragate.ai.service;

import com.auragate.common.constant.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI 异步任务生产者 — 发送 AI 任务到 RabbitMQ
 */
@Slf4j
@Service
public class AiTaskProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送 AI 任务
     *
     * @param taskId   任务 ID
     * @param userId   用户 ID
     * @param message  用户消息
     */
    public void sendTask(String taskId, Long userId, String message) {
        Map<String, Object> task = Map.of(
                "taskId", taskId,
                "userId", userId,
                "message", message,
                "timestamp", System.currentTimeMillis()
        );
        rabbitTemplate.convertAndSend(
                Constants.AI_TASK_EXCHANGE,
                Constants.AI_TASK_ROUTING_KEY,
                task
        );
        log.info("AI 任务已发送: taskId={}, userId={}", taskId, userId);
    }
}
