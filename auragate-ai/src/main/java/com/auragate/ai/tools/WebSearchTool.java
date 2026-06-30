package com.auragate.ai.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网页搜索工具 — 使用 Bing 搜索，免费无需 API Key，国内可访问
 */
public class WebSearchTool {

    // cn.bing.com 国内可访问
    private static final String BING_URL = "https://cn.bing.com/search?q=";

    // 匹配 Bing 搜索结果条目: <h2><a ... href="...">标题</a></h2>
    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "<li class=\"b_algo\"[^>]*>.*?<h2[^>]*>\\s*<a[^>]*href=\"(https?://[^\"]+)\"[^>]*>(.+?)</a>\\s*</h2>.*?<p[^>]*>(.+?)</p>",
            Pattern.DOTALL);

    public WebSearchTool() {
    }

    @Tool(description = "Search the web for information using Bing. Returns title, URL and snippet for each result.")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = BING_URL + encoded;

            HttpResponse httpResponse = HttpRequest.get(url)
                    .header("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .timeout(15000)
                    .execute();

            if (!httpResponse.isOk()) {
                return "搜索失败，HTTP 状态码: " + httpResponse.getStatus() + "。请稍后重试。";
            }

            String html = httpResponse.body();

            // 解析搜索结果
            List<String> results = new ArrayList<>();
            Matcher m = RESULT_PATTERN.matcher(html);
            int count = 0;
            while (m.find() && count < 5) {
                String link = m.group(1).trim();
                String title = m.group(2).replaceAll("<[^>]+>", "").trim();
                String snippet = m.group(3).replaceAll("<[^>]+>", "").trim();
                if (title.isEmpty()) continue;
                StringBuilder sb = new StringBuilder();
                sb.append(++count).append(". ").append(title).append("\n");
                sb.append("   链接: ").append(link).append("\n");
                sb.append("   摘要: ").append(snippet);
                results.add(sb.toString());
            }

            if (results.isEmpty()) {
                // 回退: 尝试简单匹配
                Pattern simpleP = Pattern.compile(
                        "<h2[^>]*>\\s*<a[^>]*href=\"(https?://[^\"]+)\"[^>]*>(.+?)</a>",
                        Pattern.DOTALL);
                Matcher sm = simpleP.matcher(html);
                while (sm.find() && count < 5) {
                    String link = sm.group(1).trim();
                    String title = sm.group(2).replaceAll("<[^>]+>", "").trim();
                    if (title.isEmpty()) continue;
                    results.add(++count + ". " + title + "\n   链接: " + link);
                }
            }

            return results.isEmpty()
                    ? "未找到相关搜索结果，请尝试更具体的搜索词。"
                    : String.join("\n\n", results);

        } catch (Exception e) {
            return "搜索出错: " + e.getMessage() + "。请稍后重试。";
        }
    }
}
