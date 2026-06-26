package com.auragate.common.constant;

/**
 * 通用常量
 */
public class Constants {

    // Token
    public static final String TOKEN_PREFIX = "auragate:token:";
    public static final String TOKEN_BLACKLIST_PREFIX = "auragate:token:blacklist:";
    public static final long TOKEN_EXPIRE = 24 * 60 * 60; // 24h

    // Menu cache
    public static final String MENU_CACHE_PREFIX = "auragate:menu:";
    public static final long MENU_CACHE_EXPIRE = 60 * 60; // 1h

    // AI conversation context
    public static final String AI_CONTEXT_PREFIX = "auragate:ai:context:";
    public static final long AI_CONTEXT_EXPIRE = 30 * 60; // 30min

    // Task result
    public static final String TASK_RESULT_PREFIX = "auragate:task:result:";
    public static final long TASK_RESULT_EXPIRE = 60 * 60; // 1h

    // RabbitMQ queues
    public static final String AI_TASK_QUEUE = "auragate.ai.task";
    public static final String LOG_OPERATION_QUEUE = "auragate.log.operation";
    public static final String AI_TASK_EXCHANGE = "auragate.ai.exchange";
    public static final String LOG_EXCHANGE = "auragate.log.exchange";
    public static final String LOG_OPERATION_ROUTING_KEY = "log.operation";
    public static final String AI_TASK_ROUTING_KEY = "ai.task.create";
}
