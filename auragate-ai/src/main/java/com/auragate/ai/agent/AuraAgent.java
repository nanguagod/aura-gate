package com.auragate.ai.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * AuraGate 超级智能体（拥有自主规划能力，可以直接使用）
 * Prototype 作用域 — 每次注入都创建新实例
 */
@Component
@Scope("prototype")
public class AuraAgent extends ToolCallAgent {

    public AuraAgent(ToolCallback[] allTools, @Qualifier("openAiChatModel") ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("auraAgent");

        // 当前日期信息，让模型可以直接回答"今天星期几"等简单问题，无需搜索
        LocalDate today = LocalDate.now();
        String dateInfo = String.format("今天是 %s（%s）。",
                today.format(DateTimeFormatter.ofPattern("yyyy年M月d日")),
                today.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.CHINA));

        String SYSTEM_PROMPT = """
                You are AuraAgent, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                %s

                **CRITICAL RULES:**
                1. For simple factual questions (date, time, general knowledge, definitions, calculations, translations), answer DIRECTLY without calling any tools. Only use tools when the user explicitly asks for real-time/search-based information.
                2. For search tasks: make at most 2-3 search attempts with different queries, then synthesize a comprehensive answer from the results. Do NOT keep searching with similar queries.
                3. For web scraping: scrape at most 2 pages, then summarize what you found.
                4. After gathering information, you MUST respond with a well-organized, natural-language answer to the user. Do NOT output raw tool results.
                5. When you have enough information to answer, call the `doTerminate` tool to end the interaction.
                6. NEVER repeat the same tool call with the same parameters.
                7. Always respond in Chinese unless the user uses another language.
                """.formatted(dateInfo);
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                First, decide whether the user's question can be answered directly without any tools.
                Simple facts, general knowledge, calculations, translations, dates — answer directly.
                Only use tools when: the user asks about real-time news, wants to search the web, or needs to scrape a specific URL.
                After gathering search/scrape results, synthesize a clear, natural-language answer for the user.
                If you already have enough information, respond directly to the user without calling more tools.
                Use `doTerminate` when you have provided your final answer.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(8);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .build();
        this.setChatClient(chatClient);
    }
}
