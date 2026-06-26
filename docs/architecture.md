# AuraGate 系统设计架构书

> 版本：v1.0  
> 日期：2026-06-26  
> 状态：初稿  

---

## 修订记录

| 版本 | 日期 | 描述 |
|------|------|------|
| v1.0 | 2026-06-26 | 初稿创建 |

---

## 目录

1. [项目概述](#1-项目概述)
2. [系统架构总览](#2-系统架构总览)
3. [后端架构设计](#3-后端架构设计)
4. [前端架构设计](#4-前端架构设计)
5. [数据库设计](#5-数据库设计)
6. [中间件设计](#6-中间件设计)
7. [认证与安全设计](#7-认证与安全设计)
8. [API 设计](#8-api-设计)
9. [部署架构](#9-部署架构)
10. [目录结构](#10-目录结构)

---

## 1. 项目概述

### 1.1 项目背景

AuraGate 是一个企业级内部 AI 赋能管理平台，融合了以下核心能力：

- **RBAC 权限管理**：基于 Spring Boot + Vue 3 + Element Plus 的多权限角色管理系统，包含用户管理、角色管理、菜单管理、JWT 认证
- **AI 超级智能体**：集成 AuraAgent 自主规划 Agent、RAG 知识库、多种 AI 工具、MCP 协议

整合目标是打造一个**企业级内部 AI 赋能管理平台**，将传统 RBAC 权限管理与 AI 智能体能力深度融合。

### 1.2 核心目标

| 目标 | 说明 |
|------|------|
| **统一平台** | 将两个独立项目整合为单一平台，统一认证、统一导航、统一风格 |
| **AI 赋能** | 在管理系统内集成 AI 智能体能力，支持自然语言交互与自动化任务 |
| **能力扩展** | 引入 Redis、RabbitMQ、Elasticsearch、WebSocket 增强系统能力 |
| **RAG 知识库** | 保留并扩展 RAG 技术能力，用于企业内部文档检索与知识问答 |

### 1.3 技术栈总览

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **后端框架** | Spring Boot | 3.5.x | 统一框架 |
| **JDK** | Java | 21 | LTS 版本 |
| **构建工具** | Maven | 3.9+ | 多模块管理 |
| **ORM** | MyBatis | 3.0.x | 数据库访问 |
| **安全框架** | Spring Security + JWT | - | 统一认证授权 |
| **AI 框架** | Spring AI | 1.0.x | AI 模型接入 |
| **前端框架** | Vue | 3.5+ | SPA |
| **UI 组件库** | Element Plus | 2.13+ | 企业级组件 |
| **状态管理** | Pinia | 3.x | 前端状态管理 |
| **构建工具** | Vite | 7.x | 前端构建 |

### 1.4 数据流总览

```
┌──────────────┐     HTTP/WS      ┌──────────────────┐
│  浏览器       │ ◄────────────►   │  AuraGate 后端    │
│  Vue SPA     │                  │  Spring Boot      │
│  Element Plus│                  │  8080             │
└──────────────┘                  └────────┬─────────┘
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
              ┌─────┴─────┐        ┌───────┴───────┐     ┌───────┴───────┐
              │   MySQL    │        │  PostgreSQL    │     │ Elasticsearch │
              │  3306      │        │  + PGVector    │     │  9200         │
              │ RBAC 数据   │        │  5432          │     │ 知识库全文搜索 │
              └───────────┘        │ AI 向量数据     │     │ 操作日志搜索   │
                                   └───────────────┘     └───────────────┘
              ┌───────────┐        ┌───────────────┐
              │   Redis    │        │   RabbitMQ     │
              │  6379      │        │  5672          │
              │ 缓存/会话  │        │ 异步任务队列    │
              └───────────┘        └───────────────┘
```

---

## 2. 系统架构总览

### 2.1 架构分层

```
┌─────────────────────────────────────────────────────────────────┐
│                      前端展示层 (Vue SPA)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │  系统管理模块   │  │  AI 智能体模块 │  │   知识库管理模块      │   │
│  │  用户/角色/菜单 │  │  AuraAgent 对话 │  │   RAG 文档管理       │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘   │
└─────────┼─────────────────┼─────────────────────┼───────────────┘
          │     REST API    │   WebSocket         │    REST API
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                       API 网关层                                 │
│           统一入口 /api/** 统一 JWT 拦截                          │
└─────────────────────────────┬───────────────────────────────────┘
                              │
┌─────────────────────────────┼───────────────────────────────────┐
│                    业务逻辑层 (Spring Boot)                       │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────────────┐   │
│  │ RBAC 模块   │  │ AI 模块     │  │ WebSocket 模块           │   │
│  │ 认证/用户   │  │ AuraAgent    │  │ 流式输出/任务通知        │   │
│  │ 角色/菜单   │  │ 工具调用   │  │                          │   │
│  │ 文件管理    │  │ RAG 检索   │  │                          │   │
│  └──────┬─────┘  └─────┬──────┘  └──────────────────────────┘   │
│         │               │                                        │
│  ┌──────┴─────────────────┴────────────────────────────────┐   │
│  │              公共模块 (Common)                            │   │
│  │      DTO、工具类、常量、通用配置、异常处理                  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────┬───────────────────────────────────┘
                              │
┌─────────────────────────────┼───────────────────────────────────┐
│                     数据与中间件层                               │
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  │
│  │MySQL │  │PGVec│  │  ES  │  │Redis │  │Rabbit│  │MCP   │  │
│  │RBAC  │  │tor  │  │搜索  │  │缓存  │  │MQ    │  │Server│  │
│  └──────┘  └──────┘  └──────┘  └──────┘  └──────┘  └──────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Maven 模块架构

```
auragate-parent (pom)             ← 根 POM，统一依赖版本管理
 │
 ├── auragate-common (jar)        ← 公共模块（DTO、工具类、常量、通用配置）
 │   └── 依赖：无其他模块
 │
 ├── auragate-rbac (jar)          ← RBAC 模块（用户/角色/菜单管理）
 │   ├── 依赖：common
 │   ├── config/        → SecurityConfig、ResourcesConfig、fastConfig
 │   ├── configure/     → JwtAuthenticationEntryPoint、JwtAuthenticationTokenFilter、TokenService
 │   ├── controller/    → UserController、RoleController、MenuController、LoginController 等
 │   ├── service/       → IUserService/UserServiceImpl 等
 │   ├── mapper/        → UserMapper、RoleMapper、MenuMapper 等
 │   ├── domain/        → User、Role、Menu、LoginUser 等
 │   └── resources/
 │       ├── application-rbac.yml  → MySQL 数据源、MyBatis 配置
 │       └── mapper/system/        → MyBatis XML 映射
 │
 ├── auragate-ai (jar)            ← AI 模块（AuraAgent 智能体 + RAG）
 │   ├── 依赖：common、rbac（用于用户身份对接）
 │   ├── agent/
 │   │   ├── BaseAgent.java       → 基础智能体抽象类
 │   │   ├── ReActAgent.java      → ReAct 模式智能体
 │   │   ├── ToolCallAgent.java   → 工具调用智能体
 │   │   └── AuraAgent.java         → 超级智能体（自主规划）
 │   ├── tools/
 │   │   ├── FileOperationTool.java    → 文件操作（保留）
 │   │   ├── PDFGenerationTool.java    → PDF 生成（保留）
 │   │   ├── ResourceDownloadTool.java → 资源下载（保留）
 │   │   ├── TerminalOperationTool.java→ 终端操作（保留）
 │   │   ├── WebScrapingTool.java      → 网页抓取（保留）
 │   │   ├── WebSearchTool.java        → 联网搜索（保留）
 │   │   ├── TerminateTool.java        → 终止工具（保留）
 │   │   └── ToolRegistration.java     → 工具注册中心
 │   ├── rag/
 │   │   ├── VectorStoreConfig.java         → PGVector 向量存储配置
 │   │   ├── DocumentLoader.java            → 文档加载器
 │   │   ├── TokenTextSplitter.java         → Token 文本分割器
 │   │   ├── QueryRewriter.java             → 查询重写器
 │   │   ├── KeywordEnricher.java           → 关键词增强器
 │   │   └── HybridSearchService.java       → ★ 新增：向量 + ES 混合检索
 │   ├── websocket/
 │   │   └── AiWebSocketHandler.java        → ★ 新增：WebSocket 处理 AI 流式输出
 │   ├── controller/
 │   │   └── AiController.java             → AI 对话接口（REST API）
 │   ├── service/
 │   │   └── AiConversationService.java    → ★ 新增：对话管理服务（整合 Redis 上下文）
 │   ├── config/
 │   │   ├── DashScopeConfig.java          → 阿里云百炼配置
 │   │   └── McpConfig.java                → MCP 服务配置
 │   └── resources/
 │       └── application-ai.yml            → AI 模块数据源及配置
 │
 ├── auragate-image-search-mcp (jar)  ← MCP 图片搜索服务
 │   ├── 依赖：common
 │   ├── ImageSearchTool.java
 │   └── application-mcp.yml
 │
 └── auragate-frontend/              ← 独立前端项目（不在 Maven 中）
     └── (详见前端架构设计)
```

### 2.3 模块间依赖关系图

```
auragate-common
      ↑
auragate-rbac
      ↑
auragate-ai ───────→ auragate-image-search-mcp (通过 MCP 协议调用)
```

- `auragate-ai` 依赖 `auragate-rbac`：用于获取当前登录用户信息、权限校验
- `auragate-ai` 通过 MCP 协议调用 `auragate-image-search-mcp`，非直接 Maven 依赖
- 所有模块共享 `auragate-common` 中的通用工具类

---

## 3. 后端架构设计

### 3.1 启动类与配置

**统一启动类**：`AuraGateApplication.java`

```java
@SpringBootApplication
@EnableConfigurationProperties
public class AuraGateApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuraGateApplication.class, args);
    }
}
```

**配置结构**（多 profile）：

| Profile | 文件 | 说明 |
|---------|------|------|
| default | `application.yml` | 公共配置（端口、WebSocket、通用设置） |
| rbac | `application-rbac.yml` | MySQL 数据源、MyBatis、Spring Security |
| ai | `application-ai.yml` | PGVector、DashScope、MCP |
| dev | `application-dev.yml` | 开发环境配置 |
| prod | `application-prod.yml` | 生产环境配置 |

### 3.2 RBAC 模块（原 multi-roles-project）

**保留全部原有功能**：

- 用户管理：注册、登录、个人信息、用户 CRUD
- 角色管理：角色 CRUD、角色分配
- 菜单管理：菜单树 CRUD、权限标识
- JWT 认证：Token 签发、验证、刷新、拦截
- Spring Security：接口权限控制
- 文件上传/图片预览等工具功能

**增强项**：

| 功能 | 增强内容 |
|------|---------|
| Token 存储 | 从内存/数据库改为 Redis 存储，支持分布式 |
| Token 黑名单 | 登出时将 Token 加入 Redis 黑名单，支持主动失效 |
| 菜单缓存 | 用户菜单树缓存到 Redis，减少数据库查询 |
| 操作日志 | ★ 新增：记录用户操作日志到 Elasticsearch |

### 3.3 AI 模块

#### 3.3.1 AuraAgent 超级智能体（保留）

原 AuraAgent 的完整 ReAct 工作流：

```
用户输入 → LLM 推理 → 决定行动 → 调用工具 → 观察结果 → LLM 推理 → ... → 生成最终回答
```

工作流包含：
- **思考（Thought）**：LLM 分析当前状态，决定下一步做什么
- **行动（Action）**：选择一个工具并传递参数
- **观察（Observation）**：工具执行结果
- **最终回答（Final Answer）**：达到目标后生成总结

#### 3.3.2 工具清单（全部保留）

| 工具 | 类名 | 功能 |
|------|------|------|
| 文件操作 | `FileOperationTool` | 读写文件、目录操作 |
| PDF 生成 | `PDFGenerationTool` | 生成 PDF 文档 |
| 资源下载 | `ResourceDownloadTool` | 下载网络资源 |
| 终端操作 | `TerminalOperationTool` | 执行命令行操作 |
| 网页抓取 | `WebScrapingTool` | 抓取网页内容 |
| 联网搜索 | `WebSearchTool` | 互联网搜索 |
| 图片搜索 | `ImageSearchTool`（MCP） | 通过 MCP 协议搜索图片 |
| 终止 | `TerminateTool` | 终止智能体运行 |

#### 3.3.3 RAG 知识库框架（保留并增强）

**保留的架构组件**：

| 组件 | 类名 | 说明 |
|------|------|------|
| 向量存储配置 | `VectorStoreConfig` | PGVector 连接与配置 |
| 文档加载器 | `DocumentLoader` | 支持多种格式文档加载 |
| 文本分割器 | `TokenTextSplitter` | 基于 Token 的文档分割 |
| 查询重写器 | `QueryRewriter` | 优化用户查询 |
| 关键词增强器 | `KeywordEnricher` | 扩展查询关键词 |

**新增的架构组件**：

| 组件 | 说明 |
|------|------|
| **混合检索服务** | 向量检索（PGVector）+ 全文检索（ES）双重召回，结果融合排序 |
| **文档 CRUD** | 知识库文档上传、管理、删除的管理接口 |
| **企业知识库** | 替换恋爱知识库，改为企业内部文档（制度、规范、技术文档等） |

### 3.4 WebSocket 模块（新增）

**用途**：

| 场景 | 说明 |
|------|------|
| AI 流式输出 | LLM Token 逐字推送到前端，替代原 SSE |
| 异步任务通知 | RabbitMQ 消费完成后推送结果到对应前端 |
| 任务取消 | 前端发送取消指令终止 AI 执行 |

**技术方案**：

- Spring WebSocket + STOMP 协议
- 基于 JWT Token 进行 WebSocket 连接认证
- 用户维度隔离，每个用户只能接收自己的消息

### 3.5 异步任务处理（新增）

**使用 RabbitMQ 的场景**：

| 场景 | 生产者 | 消费者 | 说明 |
|------|--------|--------|------|
| AI 耗时工具 | AI 模块 | AI 模块 | PDF 生成、网页抓取等长时间操作异步执行 |
| 操作日志 | RBAC/AI 模块 | 日志服务 | 用户操作和 AI 调用记录异步写入 ES |

**工作流程**：

```
用户请求 → 任务提交 → 返回 taskId → RabbitMQ 入队
                              →
后台消费者处理 → 完成 → 结果写入 Redis → WebSocket 通知前端
                              →
前端收到通知 → 通过 taskId 获取结果
```

---

## 4. 前端架构设计

### 4.1 技术选型

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 渐进式框架 |
| Element Plus | 2.13+ | 企业级 UI 组件库 |
| Pinia | 3.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 最新 | HTTP 请求 |
| Vite | 7.x | 构建工具 |
| @wangeditor | 最新 | 富文本编辑器 |
| SockJS + STOMP | 最新 | WebSocket 客户端 |

### 4.2 路由与菜单结构

```
AuraGate
├── 登录页          /login
├── 注册页          /register
└── 主布局（需登录）
    ├── 首页        /dashboard          ← ★ 新增：仪表盘
    ├── 系统管理     /system
    │   ├── 用户管理  /system/user       ← 原 RBAC
    │   ├── 角色管理  /system/role       ← 原 RBAC
    │   └── 菜单管理  /system/menu       ← 原 RBAC
    ├── AI 智能体    /ai
    │   └── AuraAgent 对话 /ai/agent      ← 原 AI 前端 AuraAgent 页面
    ├── 知识库管理    /knowledge          ← ★ 新增
    │   ├── 文档管理   /knowledge/docs    ← 知识库文档 CRUD
    │   └── 知识问答   /knowledge/qa      ← RAG 知识问答界面
    └── 个人中心      /profile
```

### 4.3 页面设计简述

**登录页**：原 RBAC 登录页（保留风格），支持用户名密码登录

**主布局**：原 RBAC 的左侧菜单 + 顶部栏布局，包含：
- 左侧：折叠式菜单导航
- 顶部：用户头像、个人信息、退出登录

**仪表盘**：展示系统概览信息，如用户数、角色数、AI 调用次数等

**AI 智能体页面**：类 ChatGPT 对话界面
- 对话消息列表（用户消息 + AI 回复）
- 输入框 + 发送按钮
- WebSocket 实时流式输出
- 工具调用过程可视化展示

**知识库管理**：
- 文档管理：上传、列表、删除知识库文档
- 知识问答：基于 RAG 的企业知识问答界面

### 4.4 WebSocket 集成

前端通过 SockJS + STOMP 建立 WebSocket 连接：

```
1. 登录获取 JWT Token
2. 连接 WebSocket：/ws?token={jwt}
3. 订阅用户专属队列：/user/queue/ai-response
4. 发送 AI 消息：通过 REST API 或 WebSocket 发送
5. 接收流式 Token：逐字渲染到对话界面
6. 接收任务通知：异步任务完成时接收通知
```

---

## 5. 数据库设计

### 5.1 MySQL — RBAC 业务数据（保留原结构）

保留原 `multi-roles-project` 的 5 张表：

| 表名 | 说明 |
|------|------|
| `user` | 用户表（id, username, password, nickname, email, avatar, status, create_time, update_time）|
| `role` | 角色表（id, role_name, role_key, status, create_time）|
| `menu` | 菜单表（id, menu_name, parent_id, path, component, perms, icon, order_num, menu_type, status）|
| `user_role` | 用户角色关联表 |
| `role_menu` | 角色菜单关联表 |

### 5.2 PostgreSQL + PGVector — AI 向量数据

**向量存储**：

Spring AI 的 PGVector Store 自动管理表结构，用于存储：
- 文档向量（embedding）
- 文档元数据（来源、类型、上传时间等）
- 文档内容（text）

### 5.3 Elasticsearch — 知识库全文搜索 + 操作日志

**索引规划**：

| 索引名称 | 说明 | 主要字段 |
|----------|------|---------|
| `auragate_knowledge` | 知识库文档全文索引 | title, content, category, tags, upload_time, creator |
| `auragate_operation_log` | 操作日志索引 | user_id, username, action, target, detail, ip, timestamp |

### 5.4 Redis — 缓存与状态

| Key 命名 | 类型 | 用途 | TTL |
|----------|------|------|-----|
| `auragate:token:{userId}` | String | 用户 JWT Token | 24h |
| `auragate:token:blacklist:{tokenId}` | String | Token 黑名单 | 与 Token 过期时间一致 |
| `auragate:menu:{userId}` | String (JSON) | 用户菜单树缓存 | 1h |
| `auragate:ai:context:{userId}` | String (JSON) | AI 对话上下文 | 30min |
| `auragate:task:result:{taskId}` | String (JSON) | 异步任务执行结果 | 1h |

---

## 6. 中间件设计

### 6.1 Redis

| 配置项 | 值 |
|--------|-----|
| 端口 | 6379 |
| 模式 | 单节点（开发）/ 哨兵（生产） |
| 使用场景 | Token 存储、菜单缓存、AI 上下文、任务结果缓存 |

### 6.2 RabbitMQ

| 配置项 | 值 |
|--------|-----|
| 端口 | 5672（AMQP）/ 15672（管理台） |
| 交换机类型 | Direct + Topic |

**队列定义**：

| 队列名 | 说明 | 路由 Key |
|--------|------|---------|
| `auragate.ai.task` | AI 异步任务队列 | `ai.task.*` |
| `auragate.log.operation` | 操作日志队列 | `log.operation` |

### 6.3 Elasticsearch

| 配置项 | 值 |
|--------|-----|
| 端口 | 9200（HTTP）/ 9300（TCP） |
| 版本 | 8.x |
| 使用场景 | 知识库全文检索 + 操作日志搜索 |

### 6.4 WebSocket

| 配置项 | 值 |
|--------|-----|
| 端点 | `/ws` |
| 协议 | STOMP over SockJS |
| 认证 | 连接时验证 JWT Token |

---

## 7. 认证与安全设计

### 7.1 统一 JWT 认证

```
登录流程：
1. 用户 POST /api/auth/login (username + password)
2. 后端验证凭据，生成 JWT Token
3. Token 中包含 userId、username、角色信息
4. Token 存入 Redis，有效期 24h
5. 返回 Token 给前端

请求认证流程：
1. 前端在请求头携带 Authorization: Bearer {token}
2. JwtAuthenticationTokenFilter 拦截所有 /api/** 请求
3. 验证 Token 有效性（签名、过期、Redis 黑名单校验）
4. 设置 SecurityContext，放行请求

登出流程：
1. 前端 POST /api/auth/logout
2. 后端将 Token 加入 Redis 黑名单
3. Token 在剩余有效期内不可使用
```

### 7.2 接口权限控制

```
/api/rbac/**     → 需要登录 + 角色/权限校验（Spring Security @PreAuthorize）
/api/ai/**       → 需要登录（JWT 拦截，无需额外权限校验）
/api/auth/**     → 无需登录（登录、注册接口）
/api/ws/**       → WebSocket 连接需要 JWT Token 参数
```

### 7.3 WebSocket 安全

- 建立连接时在 Query 参数中携带 JWT Token
- 后端验证 Token 后才建立连接
- 每个用户只能订阅自己的用户队列（`/user/queue/*`）

---

## 8. API 设计

### 8.1 RBAC API（保留原有）

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录 | 无需 |
| POST | `/api/auth/register` | 注册 | 无需 |
| POST | `/api/auth/logout` | 登出 | 需登录 |
| GET | `/api/user/list` | 用户列表 | 需权限 |
| POST | `/api/user` | 新增用户 | 需权限 |
| PUT | `/api/user` | 编辑用户 | 需权限 |
| DELETE | `/api/user/{id}` | 删除用户 | 需权限 |
| GET | `/api/role/list` | 角色列表 | 需权限 |
| ... | ... | 角色/菜单 CRUD | ... |

### 8.2 AI API（部分保留，部分改造）

| 方法 | 路径 | 说明 | 鉴权 | 备注 |
|------|------|------|------|------|
| POST | `/api/ai/chat` | AI 对话（同步） | 需登录 | 改造：改为 WebSocket 为主 |
| POST | `/api/ai/chat/async` | AI 对话（异步任务） | 需登录 | ★ 新增：返回 taskId |
| GET | `/api/ai/task/{taskId}` | 查询任务状态 | 需登录 | ★ 新增 |

### 8.3 WebSocket 端点

| 路径 | 说明 | 消息类型 |
|------|------|---------|
| `/ws` | WebSocket 连接端点 | - |
| `/app/ai/chat` | 发送 AI 消息 | 请求 |
| `/user/queue/ai/response` | 接收 AI 流式回复 | 推送 |
| `/user/queue/ai/task/{taskId}` | 接收异步任务完成通知 | 推送 |
| `/app/ai/cancel` | 取消正在执行的 AI 任务 | 请求 |

### 8.4 知识库 API（新增）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/knowledge/document` | 上传知识文档 |
| GET | `/api/knowledge/document/list` | 文档列表 |
| DELETE | `/api/knowledge/document/{id}` | 删除文档 |
| POST | `/api/knowledge/search` | 知识库检索（向量 + 全文混合） |
| POST | `/api/knowledge/qa` | 知识问答 |

### 8.5 操作日志 API（新增）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/log/operation/list` | 操作日志列表（支持搜索、分页） |

---

## 9. 部署架构

### 9.1 开发环境部署

```
┌─────────────────────────────────────────────────┐
│              开发机器 (Windows/Linux)              │
│                                                   │
│  AuraGate Backend (jar)  — 8080                   │
│  AuraGate Frontend (Vite Dev)  — 80              │
│                                                   │
│  MySQL        — localhost:3306                    │
│  PostgreSQL   — localhost:5432 (+ PGVector)       │
│  Redis        — localhost:6379                    │
│  RabbitMQ     — localhost:5672                    │
│  Elasticsearch— localhost:9200                    │
└─────────────────────────────────────────────────┘
```

### 9.2 生产环境部署

```
┌─────────────────────────────────────────────────┐
│                  Nginx (反向代理)                   │
│  /api/* → backend:8080                           │
│  /ws/*  → backend:8080                           │
│  /*     → frontend (静态资源)                     │
└────────────────┬────────────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
┌───┴───┐             ┌──────┴──────┐
│Backend│             │   Frontend   │
│:8080  │             │  Nginx 静态  │
└───┬───┘             └─────────────┘
    │
    ├── MySQL (独立部署)
    ├── PostgreSQL (独立部署)
    ├── Redis (独立部署)
    ├── RabbitMQ (独立部署)
    └── Elasticsearch (独立部署)
```

---

## 10. 目录结构

### 10.1 完整目录结构

```
AuraGate/
│
├── pom.xml                                    # 父 POM
├── README.md                                  # 项目说明文档
├── .gitignore
│
├── auragate-common/                           # 公共模块
│   ├── pom.xml
│   └── src/main/java/com/auragate/common/
│       ├── dto/                               # 通用 DTO
│       │   ├── AjaxResult.java
│       │   ├── PageResult.java
│       │   └── TreeSelect.java
│       ├── constant/                          # 常量
│       │   └── Constants.java
│       ├── utils/                             # 工具类
│       │   ├── SecurityUtils.java
│       │   └── StringUtils.java
│       ├── exception/                         # 异常处理
│       │   └── GlobalExceptionHandler.java
│       └── config/                            # 通用配置
│           ├── RedisConfig.java
│           ├── RabbitMqConfig.java
│           ├── ElasticsearchConfig.java
│           └── WebSocketConfig.java
│
├── auragate-rbac/                             # RBAC 模块
│   ├── pom.xml
│   └── src/main/java/com/auragate/rbac/
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── ResourcesConfig.java
│       │   └── JwtConfig.java
│       ├── configure/                         # JWT 认证组件
│       │   ├── JwtAuthenticationEntryPoint.java
│       │   ├── JwtAuthenticationTokenFilter.java
│       │   └── TokenService.java
│       ├── controller/
│       │   ├── BaseController.java
│       │   ├── LoginController.java
│       │   ├── RegisterController.java
│       │   ├── UserController.java
│       │   ├── RoleController.java
│       │   ├── MenuController.java
│       │   ├── ProfileController.java
│       │   └── FileController.java
│       ├── service/
│       │   ├── IUserService.java / UserServiceImpl.java
│       │   ├── IRoleService.java / RoleServiceImpl.java
│       │   └── IMenuService.java / MenuServiceImpl.java
│       ├── mapper/
│       │   ├── UserMapper.java
│       │   ├── RoleMapper.java
│       │   ├── MenuMapper.java
│       │   ├── UserRoleMapper.java
│       │   └── RoleMenuMapper.java
│       ├── domain/
│       │   ├── User.java
│       │   ├── Role.java
│       │   ├── Menu.java
│       │   ├── LoginUser.java
│       │   └── ...
│       └── resources/
│           ├── application-rbac.yml
│           └── mapper/system/
│               ├── UserMapper.xml
│               ├── RoleMapper.xml
│               └── MenuMapper.xml
│
├── auragate-ai/                               # AI 模块
│   ├── pom.xml
│   └── src/main/java/com/auragate/ai/
│       ├── agent/
│       │   ├── BaseAgent.java
│       │   ├── ReActAgent.java
│       │   ├── ToolCallAgent.java
│       │   └── AuraAgent.java
│       ├── tools/
│       │   ├── FileOperationTool.java
│       │   ├── PDFGenerationTool.java
│       │   ├── ResourceDownloadTool.java
│       │   ├── TerminalOperationTool.java
│       │   ├── WebScrapingTool.java
│       │   ├── WebSearchTool.java
│       │   ├── TerminateTool.java
│       │   ├── ToolRegistration.java
│       │   └── ImageSearchTool.java (MCP 客户端)
│       ├── rag/
│       │   ├── VectorStoreConfig.java
│       │   ├── DocumentLoader.java
│       │   ├── TokenTextSplitter.java
│       │   ├── QueryRewriter.java
│       │   ├── KeywordEnricher.java
│       │   └── HybridSearchService.java     ← 新增
│       ├── websocket/
│       │   └── AiWebSocketHandler.java      ← 新增
│       ├── controller/
│       │   ├── AiController.java
│       │   └── KnowledgeController.java     ← 新增
│       ├── service/
│       │   ├── AiConversationService.java   ← 新增
│       │   └── KnowledgeService.java        ← 新增
│       └── resources/
│           └── application-ai.yml
│
├── auragate-image-search-mcp/                 # MCP 图片搜索
│   ├── pom.xml
│   └── src/main/java/com/auragate/mcp/
│       ├── YuImageSearchMcpServerApplication.java
│       └── tools/
│           └── ImageSearchTool.java
│
├── auragate-frontend/                         # 前端项目
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── permission.js                      ← 路由权限守卫
│       ├── api/
│       │   ├── login.js
│       │   ├── system/ (user.js, role.js, menu.js)
│       │   ├── ai.js                          ← AI 对话 API
│       │   ├── knowledge.js                   ← 知识库 API
│       │   └── log.js                         ← 操作日志 API
│       ├── router/
│       │   └── index.js
│       ├── stores/
│       │   ├── userStore.js
│       │   └── routeStore.js
│       ├── utils/
│       │   ├── auth.js
│       │   ├── request.js
│       │   └── websocket.js                   ← ★ 新增
│       ├── views/
│       │   ├── login.vue
│       │   ├── register.vue
│       │   ├── layout/
│       │   │   ├── index.vue
│       │   │   └── components/
│       │   │       ├── AppMain.vue
│       │   │       └── Sidebar/
│       │   ├── dashboard.vue                  ← ★ 新增
│       │   ├── system/
│       │   │   ├── user/index.vue
│       │   │   ├── role/index.vue
│       │   │   ├── menu/index.vue
│       │   │   └── user/profile.vue
│       │   ├── ai/
│       │   │   └── agent.vue                  ← AI 对话页
│       │   └── knowledge/
│       │       ├── docs.vue                   ← ★ 新增
│       │       └── qa.vue                     ← ★ 新增
│       ├── components/
│       │   ├── Editor/
│       │   ├── FileUpload/
│       │   ├── IconSelect/
│       │   ├── ImagePreview/
│       │   ├── ImageUpload/
│       │   ├── Pagination/
│       │   └── SvgIcon/
│       └── assets/
│           ├── icons/
│           ├── images/
│           └── styles/
│
├── sql/                                        # 数据库脚本
│   ├── auragate_rbac.sql                      ← 原 fast.sql
│   └── auragate_init_data.sql                 ← 种子数据
│
└── docs/                                       # 文档
    └── architecture.md                        ← 本架构书
```

---

## 附录 A：保留与移除清单

### 保留的功能/文件

| 来源 | 内容 | 目标位置 |
|------|------|---------|
| multi-roles | 全部 RBAC 功能 | `auragate-rbac` |
| yu-ai-agent | AuraAgent 及所有 Agent 基类 | `auragate-ai/agent/` |
| yu-ai-agent | 全部工具类（7个） | `auragate-ai/tools/` |
| yu-ai-agent | RAG 框架（VectorStore, DocumentLoader, TokenTextSplitter, QueryRewriter, KeywordEnricher） | `auragate-ai/rag/` |
| yu-ai-agent | WebSearchTool、WebScrapingTool 等 | `auragate-ai/tools/` |
| yu-ai-agent | DashScope 配置 | `auragate-ai/config/` |
| yu-ai-agent | MCP 服务配置 | `auragate-ai/config/` |
| yu-ai-agent | 前端 ChatRoom.vue、SuperAgent.vue 等页面设计思路 | `auragate-frontend/` |
| yu-image-search-mcp | 图片搜索 MCP 服务 | `auragate-image-search-mcp/` |

### 移除的文件

| 来源 | 文件 | 原因 |
|------|------|------|
| yu-ai-agent | `LoveApp.java` | 恋爱大师核心 |
| yu-ai-agent | `LoveAppDocumentLoader.java` | 恋爱知识库专用加载器 |
| yu-ai-agent | `LoveAppRagCloudAdvisorConfig.java` | 恋爱专用配置 |
| yu-ai-agent | `LoveAppRagCustomAdvisorFactory.java` | 恋爱专用配置 |
| yu-ai-agent | `LoveAppVectorStoreConfig.java` | 恋爱专用配置 |
| yu-ai-agent | `LoveAppContextualQueryAugmenterFactory.java` | 恋爱专用配置 |
| yu-ai-agent | `MyLoggerAdvisor.java` | 可选移除（与恋爱无关，纯教学） |
| yu-ai-agent | `ReReadingAdvisor.java` | 可选移除（与恋爱无关，纯教学） |
| yu-ai-agent | `document/恋爱常见问题和回答 - 单身篇.md` | 恋爱知识库 |
| yu-ai-agent | `document/...恋爱篇.md` | 恋爱知识库 |
| yu-ai-agent | `document/...已婚篇.md` | 恋爱知识库 |
| yu-ai-agent | `FileBasedChatMemory.java` | 改用 Redis 存储对话上下文 |
| yu-ai-agent | LoveMaster.vue 及其相关前端组件 | 恋爱大师前端页面 |

### 新增的文件

| 模块 | 文件 | 说明 |
|------|------|------|
| common | `RedisConfig.java` | Redis 配置 |
| common | `RabbitMqConfig.java` | RabbitMQ 配置 |
| common | `ElasticsearchConfig.java` | ES 配置 |
| common | `WebSocketConfig.java` | WebSocket 配置 |
| ai | `AiWebSocketHandler.java` | WebSocket AI 流式处理器 |
| ai | `AiConversationService.java` | 对话管理服务 |
| ai | `KnowledgeController.java` | 知识库管理 API |
| ai | `KnowledgeService.java` | 知识库服务 |
| ai | `HybridSearchService.java` | 向量 + 全文混合检索 |
| rbac | `OperationLogAspect.java` | 操作日志切面 |
| rbac | `OperationLogController.java` | 操作日志查询 API |
| frontend | `websocket.js` | WebSocket 客户端封装 |
| frontend | `dashboard.vue` | 仪表盘 |
| frontend | `knowledge/docs.vue` | 知识库文档管理 |
| frontend | `knowledge/qa.vue` | 知识库问答页面 |
| frontend | `ai/agent.vue` | AI 智能体对话页面 |

---

## 附录 B：关键依赖（POM）

### auragate-parent/pom.xml 关键依赖

```xml
<!-- Spring Boot -->
<spring-boot.version>3.5.x</spring-boot.version>
<spring-ai.version>1.0.x</spring-ai.version>
<mybatis-spring-boot.version>3.0.x</mybatis-spring-boot.version>

<!-- 数据库 -->
<mysql-connector.version>8.x</mysql-connector.version>
<postgresql.version>42.x</postgresql.version>
<druid.version>1.2.x</druid.version>

<!-- 中间件 -->
<spring-boot-starter-data-redis>
<spring-boot-starter-amqp>  <!-- RabbitMQ -->
<spring-boot-starter-websocket>

<!-- Elasticsearch -->
<spring-boot-starter-data-elasticsearch>

<!-- AI -->
<spring-ai-alibaba-starter>  <!-- DashScope -->
<pgvector>                     <!-- PGVector 向量 -->

<!-- 工具 -->
<jjwt.version>0.12.x</jjwt.version>
<pagehelper.version>2.x</pagehelper.version>
<jsoup.version>1.18.x</jsoup.version>
<itext.version>8.x</itext.version>
<hutool.version>5.x</hutool.version>
<lombok>
<knife4j>
```
