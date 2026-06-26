package com.auragate.rbac.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.auragate.rbac.domain.AjaxResult;
import com.auragate.rbac.domain.log.OperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志查询 API
 */
@Slf4j
@RestController
@RequestMapping("/system/log")
public class LogController extends BaseController {

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 分页查询操作日志
     *
     * @param module      模块名称（可选）
     * @param operationType 操作类型（可选）
     * @param page        页码（从1开始）
     * @param pageSize    每页条数
     */
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Criteria criteria = new Criteria();

        if (module != null && !module.isEmpty()) {
            criteria = criteria.and("module").is(module);
        }
        if (operationType != null && !operationType.isEmpty()) {
            criteria = criteria.and("operationType").is(operationType);
        }

        CriteriaQuery query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(page - 1, pageSize));

        SearchHits<OperationLog> searchHits = elasticsearchTemplate.search(query, OperationLog.class);

        List<OperationLog> logs = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return success(logs);
    }
}
