package com.auragate.ai.controller;

import com.auragate.ai.service.KnowledgeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 知识库 REST API
 */
@Slf4j
@RestController
@RequestMapping("/ai/knowledge")
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "未命名文档") String title,
            @RequestParam(defaultValue = "anonymous") String author) throws IOException {
        return knowledgeService.uploadDocument(file, title, author);
    }

    /**
     * RAG 问答
     */
    @GetMapping("/qa")
    public Map<String, Object> qa(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        String answer = knowledgeService.answer(query, topK);
        return Map.of("query", query, "answer", answer);
    }

    /**
     * 文档列表
     */
    @GetMapping("/list")
    public Map<String, Object> list() {
        return Map.of("documents", knowledgeService.listDocuments());
    }
}
