package com.auragate.ai.controller;

import com.auragate.ai.agent.AuraAgent;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 流式调用 AuraAgent 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/agent/chat")
    public SseEmitter doChatWithAgent(String message) {
        AuraAgent auraAgent = new AuraAgent(allTools, dashscopeChatModel);
        return auraAgent.runStream(message);
    }
}
