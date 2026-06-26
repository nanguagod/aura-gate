package com.auragate.rbac.service.log;

import com.auragate.rbac.domain.log.OperationLog;
import com.auragate.rbac.repository.OperationLogRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 操作日志 RabbitMQ 消费者
 * 监听 auragate.log.operation 队列，将日志写入 Elasticsearch
 */
@Slf4j
@Component
@RabbitListener(queues = "${spring.rabbitmq.log.queue:auragate.log.operation}")
public class LogConsumer {

    @Resource
    private OperationLogRepository operationLogRepository;

    @RabbitHandler
    public void handleOperationLog(OperationLog operationLog) {
        try {
            operationLogRepository.save(operationLog);
            log.debug("操作日志已写入ES: {}", operationLog.getDescription());
        } catch (Exception e) {
            log.error("操作日志写入ES失败", e);
        }
    }
}
