package com.auragate.ai.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具
 */
public class WebScrapingTool {

    @Tool(description = "Scrape the text content of a web page (strips HTML tags)")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
            // 移除 script/style 标签，只提取纯文本
            document.select("script, style, nav, footer, header").remove();
            String text = document.body() != null ? document.body().text() : document.text();
            // 限制 3000 字符，避免撑爆 LLM 上下文
            if (text.length() > 3000) {
                text = text.substring(0, 3000) + "...(内容已截断)";
            }
            return text.isEmpty() ? "页面无文本内容。" : text;
        } catch (Exception e) {
            return "网页抓取出错: " + e.getMessage();
        }
    }
}
