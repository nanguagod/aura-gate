package com.auragate.ai.rag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合检索服务 — PGVector 向量检索 + Elasticsearch 全文检索 → 融合排序
 */
@Slf4j
@Service
public class HybridSearchService {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    /** ES 索引名称 */
    private static final String ES_INDEX = "auragate_knowledge";

    /**
     * 混合检索
     *
     * @param query     查询文本
     * @param topK      返回 Top N
     * @param vectorWeight 向量检索权重 (0~1)
     * @return 融合排序后的文档列表
     */
    public List<Map<String, Object>> hybridSearch(String query, int topK, double vectorWeight) {
        // 1. 向量检索
        List<Map<String, Object>> vectorResults = vectorSearch(query, topK * 2);

        // 2. 全文检索
        List<Map<String, Object>> textResults = textSearch(query, topK * 2);

        // 3. 融合排序
        return fuseResults(vectorResults, textResults, topK, vectorWeight);
    }

    /**
     * PGVector 向量检索
     */
    private List<Map<String, Object>> vectorSearch(String query, int topK) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(0.5)
                    .build();
            var responses = vectorStore.similaritySearch(request);
            List<Map<String, Object>> results = new ArrayList<>();
            for (var doc : responses) {
                Map<String, Object> item = new HashMap<>();
                item.put("content", doc.getText());
                item.put("metadata", doc.getMetadata());
                item.put("score", doc.getMetadata().getOrDefault("distance", 0.0));
                item.put("source", "vector");
                results.add(item);
            }
            return results;
        } catch (Exception e) {
            log.warn("向量检索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * Elasticsearch 全文检索
     */
    private List<Map<String, Object>> textSearch(String query, int topK) {
        try {
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index(ES_INDEX)
                    .size(topK)
                    .query(q -> q
                            .match(t -> t
                                    .field("content")
                                    .query(query)
                            )
                    ),
                    Map.class
            );

            return response.hits().hits().stream()
                    .map(hit -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("content", hit.source());
                        item.put("score", hit.score());
                        item.put("source", "es");
                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("ES 全文检索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 融合排序（加权合并）
     */
    private List<Map<String, Object>> fuseResults(
            List<Map<String, Object>> vectorResults,
            List<Map<String, Object>> textResults,
            int topK, double vectorWeight) {

        double textWeight = 1.0 - vectorWeight;

        // 归一化分数并加权
        normalizeScores(vectorResults);
        normalizeScores(textResults);

        for (Map<String, Object> item : vectorResults) {
            double score = (double) item.getOrDefault("score", 0.0);
            item.put("score", score * vectorWeight);
        }
        for (Map<String, Object> item : textResults) {
            double score = (double) item.getOrDefault("score", 0.0);
            item.put("score", score * textWeight);
        }

        // 合并
        List<Map<String, Object>> fused = new ArrayList<>(vectorResults);
        fused.addAll(textResults);

        // 按分数降序
        fused.sort((a, b) -> Double.compare(
                (double) b.getOrDefault("score", 0.0),
                (double) a.getOrDefault("score", 0.0)
        ));

        // 去重（按 content 去重）
        List<Map<String, Object>> deduped = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Map<String, Object> item : fused) {
            String content = item.getOrDefault("content", "").toString();
            if (seen.add(content)) {
                deduped.add(item);
            }
        }

        return deduped.stream().limit(topK).collect(Collectors.toList());
    }

    /**
     * Min-Max 归一化
     */
    private void normalizeScores(List<Map<String, Object>> items) {
        if (items.isEmpty()) return;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (Map<String, Object> item : items) {
            double score = (double) item.getOrDefault("score", 0.0);
            if (score < min) min = score;
            if (score > max) max = score;
        }
        double range = max - min;
        if (range == 0) return;
        for (Map<String, Object> item : items) {
            double score = (double) item.getOrDefault("score", 0.0);
            item.put("score", (score - min) / range);
        }
    }
}
