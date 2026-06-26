package com.auragate.ai.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.auragate.ai.rag.DocumentLoader;
import com.auragate.ai.rag.HybridSearchService;
import com.auragate.ai.rag.TokenTextSplitter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 知识库服务 — 文档上传/解析/分割/embedding/索引 + RAG 问答
 */
@Slf4j
@Service
public class KnowledgeService {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private TokenTextSplitter textSplitter;

    @Resource
    private DocumentLoader documentLoader;

    @Resource
    private HybridSearchService hybridSearchService;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    /** ES 知识库索引 */
    private static final String ES_INDEX = "auragate_knowledge";

    /**
     * 上传并索引文档
     *
     * @param file    上传的文件
     * @param title   文档标题
     * @param author  作者
     */
    public Map<String, Object> uploadDocument(MultipartFile file, String title, String author) throws IOException {
        // 1. 读取文件内容
        String content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);

        // 2. 分割文本
        var documents = textSplitter.splitDocuments(List.of(
                org.springframework.ai.document.Document.builder()
                        .id(UUID.randomUUID().toString())
                        .text(content)
                        .metadata(Map.of(
                                "title", title,
                                "author", author,
                                "filename", file.getOriginalFilename(),
                                "uploadTime", System.currentTimeMillis()
                        ))
                        .build()
        ));

        // 3. 写入 PGVector
        vectorStore.add(documents);

        // 4. 写入 ES
        for (var doc : documents) {
            Map<String, Object> esDoc = new HashMap<>();
            esDoc.put("content", doc.getText());
            esDoc.put("title", title);
            esDoc.put("author", author);
            esDoc.put("filename", file.getOriginalFilename());
            esDoc.put("uploadTime", System.currentTimeMillis());
            esDoc.put("chunkId", doc.getId());

            try {
                IndexResponse response = elasticsearchClient.index(i -> i
                        .index(ES_INDEX)
                        .id(doc.getId())
                        .document(esDoc)
                );
                log.debug("ES 索引成功: id={}, result={}", response.id(), response.result());
            } catch (Exception e) {
                log.warn("ES 索引失败: chunkId={}", doc.getId(), e);
            }
        }

        // 5. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("chunks", documents.size());
        result.put("title", title);
        result.put("filename", file.getOriginalFilename());
        return result;
    }

    /**
     * RAG 问答 — 混合检索 + LLM 生成
     *
     * @param query 用户问题
     * @param topK  检索文档数
     * @return 回答文本
     */
    public String answer(String query, int topK) {
        // 1. 混合检索
        var docs = hybridSearchService.hybridSearch(query, topK, 0.7);

        // 2. 构建上下文
        StringBuilder context = new StringBuilder();
        for (var doc : docs) {
            context.append(doc.getOrDefault("content", "")).append("\n\n");
        }

        // 3. 返回检索到的上下文（完整 RAG 需接入 LLM，后续扩展）
        if (context.isEmpty()) {
            return "未找到相关文档。";
        }

        return "根据知识库检索到以下相关内容：\n\n" + context.toString().trim();
    }

    /**
     * 获取所有文档列表（从 PGVector 查询）
     */
    public List<Map<String, Object>> listDocuments() {
        // 简化实现 — 后续可扩展为从数据库查询文档元数据
        return Collections.emptyList();
    }
}
