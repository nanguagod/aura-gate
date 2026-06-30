package com.auragate.ai.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.auragate.ai.rag.DocumentLoader;
import com.auragate.ai.rag.HybridSearchService;
import com.auragate.ai.rag.TokenTextSplitter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Resource
    @Qualifier("openAiChatModel")
    private ChatModel chatModel;

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
            context.append("【来源: ")
                    .append(doc.getOrDefault("source", "unknown"))
                    .append(" 相关度: ")
                    .append(String.format("%.4f", ((Number) doc.getOrDefault("score", 0.0)).floatValue()))
                    .append("】\n")
                    .append(doc.getOrDefault("content", ""))
                    .append("\n\n");
        }

        if (context.isEmpty()) {
            return "未在知识库中找到与您问题相关的文档，请尝试更换关键词或上传相关文档。";
        }

        // 3. 构建 RAG Prompt
        String systemPrompt = """
                你是一个知识库问答助手。根据用户提供的知识库文档内容回答用户的问题。

                规则：
                1. 仅根据提供的文档内容作答，不要编造文档中没有的信息
                2. 如果文档内容不足以回答，请如实告知
                3. 回答时尽量引用文档中的具体内容
                4. 使用中文回答，条理清晰
                """;

        String userPrompt = """
                用户问题：%s

                知识库检索到的相关文档内容：
                %s

                请根据以上文档内容回答用户的问题。
                """.formatted(query, context.toString().trim());

        // 4. 调用 LLM 生成回答
        try {
            var chatResponse = chatModel.call(new Prompt(
                    List.of(
                            new SystemMessage(systemPrompt),
                            new UserMessage(userPrompt)
                    )
            ));
            var output = chatResponse.getResult();
            if (output != null) {
                String answer = output.getOutput().getText();
                log.info("RAG 回答生成成功: query={}, answerLength={}", query, answer != null ? answer.length() : 0);
                return answer;
            }
        } catch (Exception e) {
            log.error("LLM 调用失败，降级为纯检索结果: query={}", query, e);
        }

        // 降级：如果 LLM 调用失败，返回检索结果原文
        return "（LLM 生成失败，以下为检索原文）\n\n" + context.toString().trim();
    }

    /**
     * 获取所有文档列表（从 ES 查询，按标题去重）
     */
    public List<Map<String, Object>> listDocuments() {
        try {
            var response = elasticsearchClient.search(s -> s
                    .index(ES_INDEX)
                    .size(1000)
                    .source(src -> src
                            .filter(f -> f.includes(List.of("title", "author", "filename", "uploadTime")))
                    ),
                    Map.class
            );

            // 按 title 去重，合并 chunks 计数
            List<Map<String, Object>> docs = new ArrayList<>();
            Set<String> seenTitles = new HashSet<>();

            for (var hit : response.hits().hits()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> source = (Map<String, Object>) hit.source();
                if (source == null) continue;
                String title = (String) source.getOrDefault("title", "");
                if (title.isEmpty() || seenTitles.contains(title)) continue;
                seenTitles.add(title);

                Map<String, Object> doc = new LinkedHashMap<>();
                doc.put("title", source.getOrDefault("title", "未知"));
                doc.put("author", source.getOrDefault("author", "未知"));
                doc.put("filename", source.getOrDefault("filename", "未知"));
                doc.put("uploadTime", source.getOrDefault("uploadTime", 0L));
                docs.add(doc);
            }

            return docs;
        } catch (Exception e) {
            log.warn("从 ES 查询文档列表失败，返回空列表", e);
            return Collections.emptyList();
        }
    }
}
