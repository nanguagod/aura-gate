package com.auragate.rbac.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.auragate.common.constant.Constants;
import com.auragate.rbac.annotation.Log;
import com.auragate.rbac.domain.log.OperationLog;
import com.auragate.rbac.utils.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志切面
 * 拦截 @Log 注解的方法，记录操作日志并发送至 RabbitMQ
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /** 记录每个线程的执行起始时间 */
    private final ThreadLocal<Long> timeLocal = new ThreadLocal<>();

    /**
     * 执行前记录起始时间
     */
    @Before(value = "@annotation(logAnnotation)")
    public void doBefore(Log logAnnotation) {
        timeLocal.set(System.currentTimeMillis());
    }

    /**
     * 正常返回后记录日志
     */
    @AfterReturning(value = "@annotation(logAnnotation)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Log logAnnotation, Object result) {
        handleLog(joinPoint, logAnnotation, null, result);
    }

    /**
     * 异常返回后记录日志
     */
    @AfterThrowing(value = "@annotation(logAnnotation)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log logAnnotation, Exception e) {
        handleLog(joinPoint, logAnnotation, e, null);
    }

    /**
     * 构建并发送操作日志
     */
    private void handleLog(JoinPoint joinPoint, Log logAnnotation, Exception e, Object result) {
        try {
            OperationLog operationLog = new OperationLog();

            // 1. 从注解获取信息
            operationLog.setModule(logAnnotation.module());
            operationLog.setOperationType(logAnnotation.operationType());
            operationLog.setDescription(logAnnotation.description());

            // 2. 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setIp(getIpAddress(request));
                operationLog.setUserAgent(request.getHeader("User-Agent"));
            }

            // 3. 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            operationLog.setMethod(method.getDeclaringClass().getName() + "." + method.getName());

            // 4. 获取请求参数
            if (logAnnotation.saveRequestData()) {
                String params = JSONUtil.toJsonStr(joinPoint.getArgs());
                // 截断过长参数
                operationLog.setRequestParams(params.length() > 2000 ? params.substring(0, 2000) : params);
            }

            // 5. 获取操作人信息
            try {
                Long userId = SecurityUtils.getUserId();
                operationLog.setOperatorId(userId);
                operationLog.setOperatorName(String.valueOf(userId));
            } catch (Exception ignored) {
                operationLog.setOperatorId(0L);
                operationLog.setOperatorName("anonymous");
            }

            // 6. 执行状态
            if (e != null) {
                operationLog.setStatus(1);
                operationLog.setErrorMsg(e.getMessage());
            } else {
                operationLog.setStatus(0);
                if (logAnnotation.saveResponseData() && result != null) {
                    operationLog.setRequestParams(JSONUtil.toJsonStr(result));
                }
            }

            // 7. 执行耗时
            Long startTime = timeLocal.get();
            operationLog.setCostTime(startTime != null ? System.currentTimeMillis() - startTime : 0L);

            // 8. 操作时间
            operationLog.setOperateTime(DateUtil.now());

            // 9. 发送到 RabbitMQ
            rabbitTemplate.convertAndSend(Constants.LOG_EXCHANGE, Constants.LOG_OPERATION_ROUTING_KEY, operationLog);
            log.debug("操作日志已发送到MQ: {}", operationLog.getDescription());
        } catch (Exception ex) {
            log.error("记录操作日志异常", ex);
        } finally {
            timeLocal.remove();
        }
    }

    /**
     * 获取请求IP
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
