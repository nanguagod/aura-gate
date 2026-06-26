# AuraGate 整合 — 执行顺序指南

> 打开 VSCode 后按此顺序依次执行。每完成一步打勾 ✅

---

## ✅ Phase 0：环境准备（一次性）

- [x] 启动 MySQL（端口 3306），创建数据库 `auragate`
- [x] 启动 PostgreSQL（端口 5432），创建数据库 `auragate_ai`，启用 pgvector 扩展
- [x] 启动 Redis（端口 6379）
- [x] 启动 RabbitMQ（端口 5672）
- [x] 启动 Elasticsearch（端口 9200）

**Docker Compose:** `docker-compose.yml` 已创建，含 postgres(pgvector)、redis、rabbitmq、elasticsearch。

---

## ✅ Phase 1：清理原始项目痕迹

**1.1 改名：com.yupi.yuaiagent → com.auragate.ai**

- [x] 修改所有 Java 文件包名：`com.yupi.yuaiagent.*` → `com.auragate.ai.*`
- [x] 重命名 `YuManus.java` → `AuraAgent.java`，更新所有引用
- [x] 重命名 `MyTokenTextSplitter.java` → `TokenTextSplitter.java`
- [x] 重命名 `MyKeywordEnricher.java` → `KeywordEnricher.java`
- [x] 重命名 `PgVectorVectorStoreConfig.java` → `VectorStoreConfig.java`
- [x] 删除：LoveApp*.java、advisor 包、chatmemory 包、demo 包、恋爱文档*.md

**1.2 改名：com.fast → com.auragate.rbac**

- [x] 修改所有 Java 文件包名：`com.fast.*` → `com.auragate.rbac.*`
- [x] 重命名 `fastConfig.java` → `AuraGateConfig.java`
- [x] 修改 MyBatis XML 中的 namespace

**1.3 清理前端品牌痕迹**

- [x] 原始前端目录已清理，等待 Phase 5 新建 Vue 3.5 + Element Plus 项目

---

## ✅ Phase 2：搭建多模块 Maven 项目

- [x] 在 AuraGate 根目录下创建 `pom.xml`（parent POM）
- [x] 创建 `auragate-common/` 模块
  - [x] pom.xml + DTO（AjaxResult, LoginUser）
  - [x] RedisConfig.java、RabbitMqConfig.java
  - [x] WebSocketConfig.java（STOMP/SockJS）、ElasticsearchConfig.java
  - [x] Constants.java（Redis key 前缀、队列名）、SecurityUtils.java
- [x] 创建 `auragate-rbac/` 模块
  - [x] pom.xml（依赖 auragate-common）
  - [x] 把改好包名的 Java 文件移入（35+ 文件）
  - [x] 把 application-rbac.yml 和 Mapper XML 移入
- [x] 创建 `auragate-ai/` 模块
  - [x] pom.xml（依赖 auragate-common + auragate-rbac + spring-ai-alibaba 等）
  - [x] 把改好包名的 Java 文件移入（22+ 文件）
  - [x] 把 application-ai.yml 移入
- [x] 创建 `auragate-image-search-mcp/` 模块
  - [x] pom.xml（依赖 auragate-common）
  - [x] 包名 `com.yupi.yuimagesearchmcpserver` → `com.auragate.mcp`
- [x] 创建统一启动类 `AuraGateApplication.java`（`scanBasePackages = "com.auragate"`）
- [x] 创建主 `application.yml`（激活 rbac, ai profile）

---

## ✅ Phase 3：RBAC 增强（Redis + 操作日志）

- [x] 改造 `TokenService.java`：Token 存 Redis（`auragate:token:{userId}`），登出加入黑名单
- [x] 改造 `JwtAuthenticationTokenFilter.java`：增加 Redis 黑名单校验 + 活跃续期
- [x] 改造 `MenuServiceImpl.java`：菜单树缓存 Redis（`auragate:menu:{userId}`，TTL 1h），增删改时清除缓存
- [x] 新建 `OperationLogAspect.java`：AOP 切面记录操作，异步发 RabbitMQ
- [x] 新建 `LogConsumer.java`：RabbitMQ 消费者 → 写 ES
- [x] 新建 `LogController.java`：ES 查询操作日志
- [x] 新建 `Log.java` 注解、`OperationLog.java` ES Document、`OperationLogRepository.java`

---

## ✅ Phase 4：AI 模块增强（已完成）

- [x] 新建 `AiConversationService.java`：Redis 存对话上下文（`auragate:ai:context:{userId}`）
- [x] 新建 `AiWebSocketHandler.java`：WebSocket 端点 `/ws/ai`，对接 AuraAgent 流式对话
- [x] 新建 `WebSocketAiConfig.java`：注册 WebSocket handler
- [x] 新建 `HybridSearchService.java`：PGVector 向量检索 + ES 全文检索 → 融合排序
- [x] 新建 `KnowledgeService.java`：文档上传/分割/embedding → PGVector + ES 双索引
- [x] 新建 `KnowledgeController.java`：文档上传、RAG 问答、文档列表 API
- [x] 新建 `AiTaskProducer.java`、`AiTaskConsumer.java`：RabbitMQ 异步 AI 任务
- [x] 启用 `VectorStoreConfig.java` 的 `@Configuration` 注解
- [x] AiController SSE 改造为 WebSocket，标记 `@Deprecated`
- [x] 编译验证 Phase 4（全部通过）

---

## □ Phase 5：前端

- [ ] 初始化 Vue 3.5 + Vite 7 + Element Plus 项目
- [ ] 配置路由：login / register / dashboard / system/* / ai/agent / knowledge/*
- [ ] 配置 Pinia Store：userStore、routeStore
- [ ] 配置 permission.js：路由权限守卫
- [ ] 搭布局组件（Sidebar、AppMain、Layout）
- [ ] 迁移登录/注册页 + 系统管理页面（user、role、menu）
- [ ] 新建 `dashboard.vue`（仪表盘）
- [ ] 新建 `ai/agent.vue`（AuraAgent 对话页面 + WebSocket 集成）
- [ ] 新建 `knowledge/docs.vue`、`knowledge/qa.vue`
- [ ] 新建 `websocket.js` 工具文件

---

## □ Phase 6：启动验证

- [ ] `mvn clean install -DskipTests` 编译通过（Phase 4 未编译）
- [ ] 启动后端：`mvn spring-boot:run`（端口 8080）
- [ ] 启动前端：`npm run dev`（端口 80）
- [ ] 测试登录/注册
- [ ] 测试用户/角色/菜单 CRUD
- [ ] 测试 AuraAgent 对话
- [ ] 测试知识库上传 + 问答
- [ ] 删除原始目录 `multi-roles-project` 和 `yu-ai-agent-master`
- [ ] `git init && git add . && git commit -m "init: AuraGate 初始化"`

---

> **参考文档：**
> - 架构设计：`docs/architecture.md`
> - 关键配置：MySQL root/123456，Redis 6379，RabbitMQ 5672，ES 9200，PG 5432
> - 编译状态：Phase 1-3 编译通过，Phase 4 代码就绪待编译验证
> - 已知问题：MySQL root 密码 123456（未处理密码加密）；AiController 仍用 SSE 未切 WebSocket
