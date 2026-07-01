







项目经历

<hr style="border: none; height: 1px; background: #dcdfe6; margin: 6px 0 16px 0;">

### 🎬 DoVideoAI——AI视频解析平台

**技术栈：** <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">SpringBoot</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">MySQL</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">Redis</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">RocketMQ</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">MyBatis-Plus</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">MinIO</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">LangChain4j</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">Vue</span>

**项目简介：** 一个集成用户鉴权、视频上传、提取音频文字及AI自动总结的全链路视频内容理解平台。针对视频处理中“长耗时阻塞”与“高并发资源冲突”痛点，基于 RocketMQ + Redisson + 分片续传 重构了系统架构，实现了大文件的稳定传输与异步处理。

- 引入 RocketMQ 将视频处理相关长耗时任务全链路异步化，将 60s+ 上传接口响应时间压缩至 50ms 以内。
- 设计 Redisson + WatchDog 分布式锁，基于MD5实现内容级去重。解决视频转码长耗时导致的锁过期问题，在测试并发场景下成功拦截重复提交，有效避免算力浪费，节省不必要的 AI Token 开销。
- 采取分片上传与断点续传机制，通过 Redis 记录分片上传状态，解决弱网环境下的连接中断超时问题，确保GB级大文件在不稳定网络下的传输可靠性。
- 基于 Redis 实现令牌桶限流，设置每秒请求上限，遏制恶意请求带来的高昂 AI Token 开销，保障服务可用性。
- 应对三方 API 网络抖动使用指数退避重试机制，兜底第三方 API 调用失败场景，显著提升任务执行成功率。
- 接入硅基流动平台大模型，使用 Redis 支持会话记忆，基于 Function Calling 实现查询信息和精准总结。



## 🏗️ AuraGate——企业级 AI 赋能管理平台

**技术栈：** <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">Spring Boot 3.5</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">Spring AI 1.0</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">Vue 3.5</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">PostgreSQL+pgvector</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">MySQL</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">Redis</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">RabbitMQ</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">Elasticsearch</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">Element Plus</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">WebSocket</span> <span style="background:#f0f2f5; padding:3px 8px; border-radius:4px; margin:0 3px; font-size:0.9em;">MCP</span>

**项目简介：** 一个集成 RBAC 权限管理、多模型 AI 智能体、RAG 知识库问答、MCP 工具服务的企业级 AI 管理平台。针对大模型回答滞后与多工具调用的稳定性问题，基于 ReAct 模式 + WebSocket 全双工通信重构了智能体对话架构，实现了多工具协同下的流式响应与对话管理。

- 基于 ReAct（Reasoning + Acting）模式自研 Agent 状态机，实现 Think → Act → Observe 循环，支持 7 种工具（网页搜索、网页抓取、文件读写、资源下载、终端命令、PDF 生成、终止信号）的手动编排，通过 ToolCallingManager 统一调度工具执行结果回注到对话上下文，解决了 Spring AI 默认自动执行工具导致的上下文不可控问题。

- 构建 WebSocket 流式对话通道，通过 `CompletableFuture.runAsync()` 将 Agent 推理循环异步化，结合 JWT 握手拦截器从 URL 查询参数中提取用户身份，实现单次连接内多轮对话的全双工推送，中间推理步骤静默执行、仅在最终答案生成后一次性推送，避免了中间工具调用结果污染前端界面。

- 设计混合检索引擎（Hybrid Search），并行执行 PGVector 向量检索（余弦相似度，HNSW 索引）与 Elasticsearch 全文检索，通过 Min-Max 归一化 + 加权融合（向量权重 0.7）对结果重排序并去重，集成 DashScope 大模型进行 Query Rewriting 查询改写与 Keyword 关键词增强，提升了 RAG 问答的召回精度。

- 实现 JWT + Redis 双层 Token 认证体系：TokenService 管理 Token 的创建、刷新与销毁全生命周期，每次请求校验 Redis 中 Token 一致性防止盗用，登出时将 Token 加入黑名单并设置与原始过期时间一致的 TTL 实现自动清理。

- 基于 RabbitMQ 构建异步任务队列，将 AI 任务下发与操作日志记录全链路异步化，AI 任务结果写入 Redis 并设置 1 小时 TTL 自动过期，操作日志通过 AOP 切面捕获后投递至 Elasticsearch 持久化，实现审计日志与业务逻辑的解耦。

- 接入 DeepSeek 与阿里通义千问双模型，通过 Spring AI OpenAI-compatible 客户端统一调用 DeepSeek API，DashScope 提供 Embedding 向量化能力，支持模型配置热切换，覆盖对话推理、向量嵌入、关键词提取、查询改写等多种 AI 场景。

- 基于 Spring AI MCP 协议构建以图搜图微服务，通过 Pexels API 提供自然语言图片检索能力，以独立端口暴露 MCP Server 端点供主应用消费，实现工具能力的模块化解耦与跨进程调用。
