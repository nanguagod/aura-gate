package com.auragate.rbac.repository;

import com.auragate.rbac.domain.log.OperationLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 操作日志 ES 仓库
 */
@Repository
public interface OperationLogRepository extends ElasticsearchRepository<OperationLog, String> {

    /**
     * 按操作人ID查询，按操作时间降序
     */
    List<OperationLog> findByOperatorIdOrderByOperateTimeDesc(Long operatorId);

    /**
     * 按模块查询，按操作时间降序
     */
    List<OperationLog> findByModuleOrderByOperateTimeDesc(String module);
}
