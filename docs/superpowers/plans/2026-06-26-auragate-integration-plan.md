# AuraGate 整合实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 RBAC 管理系统 + AI 超级智能体整合为统一平台 AuraGate，彻底清理原始项目痕迹，并引入新中间件

**Architecture:** 多模块 Maven 项目（common → rbac → ai）+ 独立 Vue 3.5 前端，统一 JWT 认证，MySQL+PGVector+ES 三数据源，集成 Redis/RabbitMQ/WebSocket

**Tech Stack:** Spring Boot 3.5.x, Java 21, MyBatis, Spring Security+JWT, Spring AI, Vue 3.5+Element Plus, Pinia, Vite 7, Redis, RabbitMQ, Elasticsearch, WebSocket

---

## Phase 0: 环境准备与项目脚手架

### Task 0.1: 安装并启动中间件

- [ ] **Step 1: 安装并启动 MySQL 8.x**
  - 确保 MySQL 运行在 3306 端口
  - 创建数据库 `auragate`（替代原 `fast`）
  - 执行建表脚本（从原 `fast.sql` 迁移）

- [ ] **Step 2: 安装并启动 PostgreSQL 16 + pgvector**
  - 确保 PostgreSQL 运行在 5432 端口
  - 创建数据库 `auragate_ai`
  - 启用 pgvector 扩展

- [ ] **Step 3: 安装并启动 Redis**
  - 确保 Redis 运行在 6379 端口

- [ ] **Step 4: 安装并启动 RabbitMQ**
  - 确保 RabbitMQ 运行在 5672 端口（管理台 15672）

- [ ] **Step 5: 安装并启动 Elasticsearch 8.x**
  - 确保 ES 运行在 9200 端口

### Task 0.2: 创建项目目录结构

- [ ] **Step 1: 创建 AuraGate 根目录结构**

在 `d:\alldesk\code\AuraGate` 下创建多模块目录：

```
auragate-parent/                 ← 临时目录，稍后初始化
    pom.xml
    auragate-common/
        pom.xml
        src/main/java/com/auragate/common/
        src/main/resources/
    auragate-rbac/
        pom.xml
        src/main/java/com/auragate/rbac/
        src/main/resources/
    auragate-ai/
        pom.xml
        src/main/java/com/auragate/ai/
        src/main/resources/
    auragate-image-search-mcp/
        pom.xml
        src/main/java/com/auragate/mcp/
        src/main/resources/
    auragate-frontend/
        (将在 Phase 5 创建)
    sql/
    docs/
```

- [ ] **Step 2: 建立 git 仓库**

```bash
cd d:\alldesk\code\AuraGate
git init
```

---

## Phase 1: 清理原始项目痕迹（改造旧代码）

> 此阶段直接在原项目源码上做批量改名操作，代码移动到新目录之前完成清理。

### Task 1.1: 清理 yu-ai-agent-master 的包名和类名

**说明：** 将 `com.yupi.yuaiagent` 包重命名为 `com.auragate.ai`，将 `YuManus` 类重命名为 `AuraAgent`

**涉及文件：** 约 55 个 Java 文件 + 配置文件

- [ ] **Step 1: 创建包路径映射**

```
com.yupi.yuaiagent                   → com.auragate.ai
com.yupi.yuaiagent.agent             → com.auragate.ai.agent
com.yupi.yuaiagent.agent.model       → com.auragate.ai.agent.model
com.yupi.yuaiagent.controller        → com.auragate.ai.controller
com.yupi.yuaiagent.config            → com.auragate.ai.config
com.yupi.yuaiagent.constant          → com.auragate.ai.constant
com.yupi.yuaiagent.tools             → com.auragate.ai.tools
com.yupi.yuaiagent.rag               → com.auragate.ai.rag
com.yupi.yuaiagent.service           → com.auragate.ai.service  (新增)
com.yupi.yuaiagent.websocket         → com.auragate.ai.websocket (新增)
com.yupi.yuaiagent.demo              → 删除（demo 示例代码不保留）
```

- [ ] **Step 2: 重命名类**

| 原名 | 新名 | 说明 |
|------|------|------|
| `YuManus.java` | `AuraAgent.java` | 核心智能体类 |
| `YuAiAgentApplication.java` | 合并到主启动类 | 不再独立存在 |
| `MyTokenTextSplitter.java` | `TokenTextSplitter.java` | 去掉 My 前缀 |
| `MyKeywordEnricher.java` | `KeywordEnricher.java` | 去掉 My 前缀 |
| `PgVectorVectorStoreConfig.java` | `VectorStoreConfig.java` | 简化命名 |
| 所有 LoveApp* 文件 | 删除 | 恋爱大师相关 |
| 所有 demo/ 包文件 | 删除 | 示例代码 |
| `FileBasedChatMemory.java` | 删除 | 改用 Redis |
| `CorsConfig.java` | 保留 | 整合到 common 配置 |
| `MyLoggerAdvisor.java` | 删除 | 恋爱相关 |
| `ReReadingAdvisor.java` | 删除 | 恋爱相关 |

- [ ] **Step 3: 修改 pom.xml 中的 groupId 和 artifactId**

```xml
<!-- 原来 -->
<groupId>com.yupi</groupId>
<artifactId>yu-ai-agent</artifactId>

<!-- 改为 -->
<groupId>com.auragate</groupId>
<artifactId>auragate-ai</artifactId>
```

- [ ] **Step 4: 修改 application.yml 中的包扫描路径**

```yaml
# 原来
packages-to-scan: com.yupi.yuaiagent.controller

# 改为
packages-to-scan: com.auragate.ai.controller
```

### Task 1.2: 清理 multi-roles-project 的包名和配置

**说明：** 将 `com.fast` 包重命名为 `com.auragate.rbac`

**涉及文件：** 约 40 个 Java 文件 + application.yml

- [ ] **Step 1: 创建包路径映射**

```
com.fast                             → com.auragate.rbac
com.fast.system.config               → com.auragate.rbac.config
com.fast.system.configure            → com.auragate.rbac.configure
com.fast.system.controller           → com.auragate.rbac.controller
com.fast.system.service              → com.auragate.rbac.service
com.fast.system.service.impl         → com.auragate.rbac.service.impl
com.fast.system.mapper               → com.auragate.rbac.mapper
com.fast.system.domain               → com.auragate.rbac.domain
com.fast.system.domain.vo            → com.auragate.rbac.domain.vo
com.fast.system.utils                → com.auragate.rbac.utils
com.fast.system.exception            → com.auragate.rbac.exception
com.fast.system.constants            → com.auragate.rbac.constants
```

- [ ] **Step 2: 重命名类和配置文件**

| 原名 | 新名 | 说明 |
|------|------|------|
| `SpringbootApplication.java` | 合并到主启动类 | 不再独立存在 |
| `fastConfig.java` | `AuraGateConfig.java` | 系统配置类 |
| 数据库名 `fast` | `auragate` | application.yml 中的 JDBC URL |
| MyBatis `com.fast.**.domain` | `com.auragate.rbac.domain` | type-aliases-package |
| 日志 `com.fast: debug` | `com.auragate.rbac: debug` | 日志级别配置 |

- [ ] **Step 3: 修改 pom.xml**

```xml
<!-- 原来 -->
<groupId>com.fast</groupId>
<artifactId>fast</artifactId>

<!-- 改为 -->
<groupId>com.auragate</groupId>
<artifactId>auragate-rbac</artifactId>
```

### Task 1.3: 清理 MCP 图片搜索服务的包名

- [ ] **Step 1: 重命名包**

```
com.yupi.yuimagesearchmcpserver   → com.auragate.mcp
```

- [ ] **Step 2: 修改 pom.xml**

```xml
<groupId>com.auragate</groupId>
<artifactId>auragate-image-search-mcp</artifactId>
```

### Task 1.4: 清理前端品牌痕迹

**涉及文件：** Home.vue, SuperAgent.vue, LoveMaster.vue, AppFooter.vue, router/index.js, index.html

- [ ] **Step 1: 修改所有"鱼皮AI超级智能体应用平台"为"AuraGate"**

批量替换规则：

| 原文字 | 替换为 |
|--------|--------|
| `鱼皮AI超级智能体` | `AuraGate` |
| `鱼皮AI超级智能体应用平台` | `AuraGate` |
| `AI超级智能体是鱼皮AI超级智能体应用平台的全能助手` | `AuraAgent 是 AuraGate 的全能 AI 助手` |
| `站长：鱼皮` | 删除该行 |
| `© {year} 鱼皮AI超级智能体应用平台 - 让AI为你服务` | `© {year} AuraGate - 企业级 AI 赋能平台` |
| meta author = `鱼皮` | 删除或改为 `AuraGate` |
| CSS content `'鱼皮AI超级智能体'` | 删除该 CSS 规则 |

- [ ] **Step 2: 修改 index.html**

```html
<title>AuraGate - 企业级 AI 赋能管理平台</title>
<meta name="description" content="AuraGate 企业级 AI 赋能管理平台，集成 YuManus 超级智能体、RAG 知识库等 AI 能力" />
<meta name="keywords" content="AuraGate,AI智能体,企业平台,AI助手" />
```

- [ ] **Step 3: 修改 router/index.js 中的 meta 描述**

- [ ] **Step 4: 删除 LoveMaster.vue（恋爱大师页面）**

- [ ] **Step 5: 修改 package.json 中的 name**

```json
{
  "name": "auragate-frontend"
}
```

- [ ] **Step 6: 清理 README.md**

删除或重写根 README.md，去除所有"鱼皮"、"yupi"、"编程导航"、"liyupi"等品牌信息，改为 AuraGate 项目介绍。

---

## Phase 2: auragate-common 公共模块

### Task 2.1: 创建父 POM

- [ ] **Step 1: 创建 auragate-parent/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.auragate</groupId>
    <artifactId>auragate-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <name>AuraGate</name>
    <description>企业级 AI 赋能管理平台</description>

    <modules>
        <module>auragate-common</module>
        <module>auragate-rbac</module>
        <module>auragate-ai</module>
        <module>auragate-image-search-mcp</module>
    </modules>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.9</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
        <mybatis-spring-boot.version>3.0.5</mybatis-spring-boot.version>
        <jjwt.version>0.12.6</jjwt.version>
        <pagehelper.version>2.1.0</pagehelper.version>
        <jsoup.version>1.18.3</jsoup.version>
        <itext.version>8.0.5</itext.version>
        <hutool.version>5.8.34</hutool.version>
        <druid.version>1.2.24</druid.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

### Task 2.2: 创建 common 模块

- [ ] **Step 1: 创建 auragate-common/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.auragate</groupId>
        <artifactId>auragate-parent</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>auragate-common</artifactId>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- RabbitMQ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>

        <!-- WebSocket -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>

        <!-- Elasticsearch -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Hutool -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: 创建通用 DTO**

创建 `com.auragate.common.dto` 包，包含：

| 类名 | 说明 |
|------|------|
| `AjaxResult.java` | 从原 com.fast.system.domain 迁移，统一 API 响应格式 |
| `PageResult.java` | 分页结果封装 |
| `TreeSelect.java` | 树形选择组件（从原 RBAC 迁移）|

- [ ] **Step 3: 创建常量类**

`com.auragate.common.constant.Constants.java`
- Token 前缀、Redis Key 命名常量、角色 ID 常量等

- [ ] **Step 4: 创建工具类**

`com.auragate.common.utils` 包：
| 类名 | 来源 | 说明 |
|------|------|------|
| `SecurityUtils.java` | 从原 `com.fast.system.utils` 迁移 | 用户认证工具类 |
| `StringUtils.java` | 新增 | 字符串工具类 |

- [ ] **Step 5: 创建全局异常处理器**

`com.auragate.common.exception.GlobalExceptionHandler.java`
- 从原 `com.fast.system.exception` 迁移，统一异常处理

- [ ] **Step 6: 创建 Redis 配置**

`com.auragate.common.config.RedisConfig.java`
- Redis 连接配置
- RedisTemplate Bean 配置（Key: String, Value: JSON 序列化）

- [ ] **Step 7: 创建 RabbitMQ 配置**

`com.auragate.common.config.RabbitMqConfig.java`
- 声明交换机、队列、绑定关系
- 队列：`auragate.ai.task`（AI 异步任务）、`auragate.log.operation`（操作日志）

```java
@Configuration
public class RabbitMqConfig {
    public static final String AI_TASK_QUEUE = "auragate.ai.task";
    public static final String LOG_OPERATION_QUEUE = "auragate.log.operation";
    public static final String AI_TASK_EXCHANGE = "auragate.ai.exchange";
    public static final String LOG_EXCHANGE = "auragate.log.exchange";

    @Bean
    public Queue aiTaskQueue() {
        return new Queue(AI_TASK_QUEUE, true);
    }

    @Bean
    public Queue logOperationQueue() {
        return new Queue(LOG_OPERATION_QUEUE, true);
    }

    @Bean
    public DirectExchange aiTaskExchange() {
        return new DirectExchange(AI_TASK_EXCHANGE);
    }

    @Bean
    public Binding aiTaskBinding() {
        return BindingBuilder.bind(aiTaskQueue()).to(aiTaskExchange()).with("ai.task.create");
    }
}
```

- [ ] **Step 8: 创建 WebSocket 配置**

`com.auragate.common.config.WebSocketConfig.java`
- Spring WebSocket + STOMP 配置
- 注册 `/ws` 端点，启用 SockJS
- 配置消息代理前缀 `/user/queue`、`/topic`

- [ ] **Step 9: 创建 Elasticsearch 配置**

`com.auragate.common.config.ElasticsearchConfig.java`
- ES 连接配置（host: localhost, port: 9200）
- RestClient Bean 配置

---

## Phase 3: auragate-rbac RBAC 模块

### Task 3.1: 创建 rbac 模块目录并迁移代码

- [ ] **Step 1: 创建 auragate-rbac/pom.xml**

从原 `multi-roles-project/fast/springboot/pom.xml` 改造：
- 父 POM 改为 `auragate-parent`
- groupId 改为 `com.auragate`
- artifactId 改为 `auragate-rbac`
- 依赖 `auragate-common`
- 保留：spring-boot-starter-web, mybatis, mysql-connector, druid, pagehelper, spring-security, validation

- [ ] **Step 2: 迁移并重命名 Java 源文件**

将原 `com.fast` 包下所有 Java 文件迁移到 `com.auragate.rbac` 包，保留目录结构：

```
com.fast.system.config.ApplicationConfig.java       → com.auragate.rbac.config.ApplicationConfig.java
com.fast.system.config.ResourcesConfig.java          → com.auragate.rbac.config.ResourcesConfig.java
com.fast.system.config.SecurityConfig.java           → com.auragate.rbac.config.SecurityConfig.java
com.fast.system.config.fastConfig.java               → com.auragate.rbac.config.AuraGateConfig.java
com.fast.system.configure.*                          → com.auragate.rbac.configure.*
com.fast.system.controller.*                         → com.auragate.rbac.controller.*
com.fast.system.service.*                            → com.auragate.rbac.service.*
com.fast.system.mapper.*                             → com.auragate.rbac.mapper.*
com.fast.system.domain.*                             → com.auragate.rbac.domain.*
com.fast.system.utils.*                              → com.auragate.rbac.utils.*
com.fast.system.exception.*                          → 已迁移到 common
com.fast.system.constants.*                          → com.auragate.rbac.constants.*
```

- [ ] **Step 3: 迁移 MyBatis XML 映射文件**

```
src/main/resources/mapper/system/UserMapper.xml      → auragate-rbac/src/main/resources/mapper/rbac/UserMapper.xml
src/main/resources/mapper/system/RoleMapper.xml      → auragate-rbac/src/main/resources/mapper/rbac/RoleMapper.xml
src/main/resources/mapper/system/MenuMapper.xml      → auragate-rbac/src/main/resources/mapper/rbac/MenuMapper.xml
src/main/resources/mapper/system/UserRoleMapper.xml  → auragate-rbac/src/main/resources/mapper/rbac/UserRoleMapper.xml
src/main/resources/mapper/system/RoleMenuMapper.xml  → auragate-rbac/src/main/resources/mapper/rbac/RoleMenuMapper.xml
```

更新 XML 中的 `namespace` 属性指向新的包名。

- [ ] **Step 4: 修改 application-rbac.yml**

从原 `application.yml` 改造：
- 数据库名 `fast` → `auragate`
- 包扫描路径 `com.fast` → `com.auragate.rbac`
- MyBatis type-aliases-package 更新
- 日志配置更新

```yaml
server:
  port: 8080

spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/auragate?useUnicode=true&characterEncoding=utf8
      username: root
      password: your_password

# MyBatis配置
mybatis:
  type-aliases-package: com.auragate.rbac.domain
  mapper-locations: classpath:mapper/rbac/*.xml

# 日志
logging:
  level:
    com.auragate.rbac: debug
```

- [ ] **Step 5: 改造 TokenService 使用 Redis**

修改 `JwtAuthenticationTokenFilter.java` 和 `TokenService.java`：
- Token 从内存 Map 存储改为 Redis 存储
- 登录成功后将 Token 写入 Redis，Key = `auragate:token:{userId}`
- 登出时将 Token 加入 Redis 黑名单，Key = `auragate:token:blacklist:{tokenId}`
- 请求校验时检查 Redis 黑名单

### Task 3.2: 增强 RBAC 模块

- [ ] **Step 1: 添加菜单缓存到 Redis**

修改 `MenuServiceImpl.java`：
- 用户菜单树查询结果缓存到 Redis（Key = `auragate:menu:{userId}`，TTL = 1h）
- 菜单发生增删改时清除对应缓存

- [ ] **Step 2: 创建操作日志切面**

`com.auragate.rbac.aspect.OperationLogAspect.java`
- 环绕切面，拦截所有 Controller 方法
- 记录：用户 ID、操作类型、请求参数、执行时间、IP 地址
- 异步发送到 RabbitMQ 日志队列

```java
@Aspect
@Component
public class OperationLogAspect {
    @Around("execution(* com.auragate.rbac.controller.*.*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        // 执行原方法
        // 记录结束时间
        // 构建日志对象
        // 发送到 RabbitMQ 日志队列
    }
}
```

- [ ] **Step 3: 创建操作日志消费者和查询 API**

- `com.auragate.rbac.service.log.LogConsumer.java` — RabbitMQ 消费者，将日志写入 ES
- `com.auragate.rbac.controller.LogController.java` — 操作日志查询 API（从 ES 检索）

---

## Phase 4: auragate-ai AI 模块

### Task 4.1: 创建 ai 模块目录并迁移代码

- [ ] **Step 1: 创建 auragate-ai/pom.xml**

从原 `yu-ai-agent-master/pom.xml` 改造：
- 父 POM 改为 `auragate-parent`
- 依赖 `auragate-common` 和 `auragate-rbac`
- 保留：spring-ai-alibaba-starter, pgvector, langchain4j, jsoup, itext, hutool
- 新增：spring-boot-starter-websocket（WebSocket 流式输出）

- [ ] **Step 2: 迁移 Agent 相关代码（保留）**

将原 `com.yupi.yuaiagent.agent` 包中的文件迁移到 `com.auragate.ai.agent`：

```
YuManus.java        → AuraAgent.java
ToolCallAgent.java  → ToolCallAgent.java（保留原名）
ReActAgent.java     → ReActAgent.java（保留原名）
BaseAgent.java      → BaseAgent.java（保留原名）
AgentState.java     → AgentState.java（保留原名）
```

重命名所有 `YuManus` 引用为 `AuraAgent`：
- 类声明：`public class AuraAgent extends ToolCallAgent`
- 变量名：`yuManus` → `auraAgent`
- 注释中的引用更新

- [ ] **Step 3: 迁移工具类代码（全部保留）**

将 `com.yupi.yuaiagent.tools` → `com.auragate.ai.tools`

| 文件 | 说明 |
|------|------|
| `FileOperationTool.java` | 保留 |
| `PDFGenerationTool.java` | 保留 |
| `ResourceDownloadTool.java` | 保留 |
| `TerminalOperationTool.java` | 保留 |
| `WebScrapingTool.java` | 保留 |
| `WebSearchTool.java` | 保留 |
| `TerminateTool.java` | 保留 |
| `ToolRegistration.java` | 保留，更新包名引用 |

- [ ] **Step 4: 迁移 RAG 框架代码（保留核心组件）**

将 `com.yupi.yuaiagent.rag` → `com.auragate.ai.rag`

**保留：**
| 原名 | 新名 |
|------|------|
| `PgVectorVectorStoreConfig.java` | `VectorStoreConfig.java` |
| `LoveAppDocumentLoader.java` → 保留逻辑 | `DocumentLoader.java` |
| `MyTokenTextSplitter.java` | `TokenTextSplitter.java` |
| `QueryRewriter.java` | 保留原名 |
| `MyKeywordEnricher.java` | `KeywordEnricher.java` |

**删除：**
| 文件 | 原因 |
|------|------|
| `LoveAppRagCloudAdvisorConfig.java` | 恋爱专用 |
| `LoveAppRagCustomAdvisorFactory.java` | 恋爱专用 |
| `LoveAppVectorStoreConfig.java` | 恋爱专用（与 PgVectorVectorStoreConfig 重复）|
| `LoveAppContextualQueryAugmenterFactory.java` | 恋爱专用 |

- [ ] **Step 5: 删除恋爱大师相关代码**

| 文件 | 操作 |
|------|------|
| `LoveApp.java` | 删除 |
| 所有 `love` 相关文档（3 个 markdown 文件）| 删除 |
| `FileBasedChatMemory.java` | 删除（改用 Redis）|
| `MyLoggerAdvisor.java` | 删除 |
| `ReReadingAdvisor.java` | 删除 |
| `ChatMemory` 相关配置 | 删除 |
| `demo/` 包中所有文件 | 删除 |

- [ ] **Step 6: 迁移控制器和配置**

将 `com.yupi.yuaiagent.controller` → `com.auragate.ai.controller`：
- `AiController.java` — 保留并改造
- `HealthController.java` — 保留
- 删除：LoveMaster 相关的控制器（无单独控制器，已包含在 AiController 中）

将 `com.yupi.yuaiagent.config` → `com.auragate.ai.config`：
- `CorsConfig.java` → 整合到 common 或保留
- DashScope/MCP 相关配置

- [ ] **Step 7: 创建 application-ai.yml**

```yaml
spring:
  ai:
    dash-scope:
      api-key: ${DASHSCOPE_API_KEY}
  datasource:
    url: jdbc:postgresql://localhost:5432/auragate_ai
    username: postgres
    password: your_password

# AI 模块配置
ai:
  mcp:
    image-search:
      url: http://localhost:8082
```

### Task 4.2: 新增 WebSocket 流式输出

- [ ] **Step 1: 创建 AiWebSocketHandler.java**

`com.auragate.ai.websocket.AiWebSocketHandler.java`

```java
@Component
public class AiWebSocketHandler extends TextWebSocketHandler {
    // 管理用户 WebSocket 连接会话
    // 接收用户消息后调用 LLM 流式生成
    // 逐 Token 推送到前端
    // 支持取消操作
}
```

- [ ] **Step 2: 改造 AiController.java**

- 保留 REST API 入口（`POST /api/ai/chat`）
- 新增 WebSocket 消息处理（在 WebSocket Handler 中处理 `/app/ai/chat`）
- 新增异步任务入口（`POST /api/ai/chat/async` → 返回 taskId → 放入 RabbitMQ）

### Task 4.3: 新增对话管理服务

- [ ] **Step 1: 创建 AiConversationService.java**

`com.auragate.ai.service.AiConversationService.java`
- 管理对话上下文：从 Redis 读取/写入对话历史
- Key = `auragate:ai:context:{userId}`，TTL = 30min
- 上下文裁剪（超出 Token 限制时丢弃最早的消息）

### Task 4.4: 新增混合检索服务

- [ ] **Step 1: 创建 HybridSearchService.java**

`com.auragate.ai.rag.HybridSearchService.java`
- 向量检索（PGVector）：根据 query embedding 搜索相似文档
- 全文检索（ES）：根据关键词搜索匹配文档
- 结果融合：两种检索结果按相关性加权排序，返回 Top-N

```java
@Service
public class HybridSearchService {
    // search(query) → 同时调用向量检索 + 全文检索
    // 融合排序：score = α * vectorScore + (1-α) * textScore
    // 返回合并结果列表
}
```

### Task 4.5: 新增知识库管理

- [ ] **Step 1: 创建 KnowledgeController.java**

`com.auragate.ai.controller.KnowledgeController.java`
- `POST /api/knowledge/document` — 上传文档（存储到 PGVector + 索引到 ES）
- `GET /api/knowledge/document/list` — 文档列表
- `DELETE /api/knowledge/document/{id}` — 删除文档（从 PGVector 和 ES 同时删除）
- `POST /api/knowledge/search` — 混合检索
- `POST /api/knowledge/qa` — RAG 知识问答

- [ ] **Step 2: 创建 KnowledgeService.java**

`com.auragate.ai.service.KnowledgeService.java`
- 文档加载：上传 → 解析（TXT/PDF/Markdown）→ 分割 → 生成 embedding → 存入 PGVector
- 文档索引：文档内容同步索引到 Elasticsearch
- 知识问答：用户问题 → 混合检索 → LLM 生成回答

### Task 4.6: 异步任务处理

- [ ] **Step 1: 创建 RabbitMQ 生产者和消费者**

- `com.auragate.ai.service.AiTaskProducer.java` — 发送 AI 异步任务到 RabbitMQ
- `com.auragate.ai.service.AiTaskConsumer.java` — 消费 AI 任务，处理完成后通过 WebSocket 通知前端

```java
@Component
@RabbitListener(queues = "auragate.ai.task")
public class AiTaskConsumer {
    @RabbitHandler
    public void handleTask(AiTaskMessage message) {
        // 执行 AI 任务
        // 结果写入 Redis (auragate:task:result:{taskId})
        // 通过 WebSocket 通知前端
    }
}
```

---

## Phase 5: auragate-frontend 前端

### Task 5.1: 初始化前端项目

- [ ] **Step 1: 创建 Vue 3.5 + Vite 7 项目**

```bash
cd d:\alldesk\code\AuraGate
npm create vite@latest auragate-frontend -- --template vue
cd auragate-frontend
```

- [ ] **Step 2: 安装依赖**

```bash
npm install element-plus@latest pinia@latest vue-router@4 axios@latest
npm install @wangeditor/editor @wangeditor/editor-for-vue
npm install sockjs-client @stomp/stompjs
```

- [ ] **Step 3: 配置 vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 80,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
})
```

### Task 5.2: 搭建前端框架

- [ ] **Step 1: 复制布局组件**

从原 `multi-roles-project/fast/vue/src` 迁移：
- `views/layout/index.vue` — 主布局（左侧菜单 + 顶部栏 + 内容区）
- `views/layout/components/AppMain.vue`
- `views/layout/components/Sidebar/*.vue` — 侧边栏菜单组件

- [ ] **Step 2: 配置路由**

`src/router/index.js`：

```javascript
const routes = [
  { path: '/login', component: () => import('@/views/login.vue') },
  { path: '/register', component: () => import('@/views/register.vue') },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/dashboard.vue') },
      {
        path: 'system',
        redirect: '/system/user',
        children: [
          { path: 'user', component: () => import('@/views/system/user/index.vue') },
          { path: 'role', component: () => import('@/views/system/role/index.vue') },
          { path: 'menu', component: () => import('@/views/system/menu/index.vue') },
          { path: 'user/profile', component: () => import('@/views/system/user/profile.vue') }
        ]
      },
      {
        path: 'ai',
        children: [
          { path: 'agent', component: () => import('@/views/ai/agent.vue') }
        ]
      },
      {
        path: 'knowledge',
        children: [
          { path: 'docs', component: () => import('@/views/knowledge/docs.vue') },
          { path: 'qa', component: () => import('@/views/knowledge/qa.vue') }
        ]
      }
    ]
  }
]
```

- [ ] **Step 3: 配置 Pinia Store**

从原 RBAC 前端迁移：
- `src/stores/userStore.js` — 用户状态（Token、用户信息、菜单）
- `src/stores/routeStore.js` — 动态路由

- [ ] **Step 4: 配置权限守卫**

从原 RBAC 前端迁移 `src/permission.js`：
- 路由跳转前检查登录状态
- 未登录重定向到 /login
- 动态生成菜单路由

### Task 5.3: 迁移 RBAC 页面

- [ ] **Step 1: 迁移登录页**

从 `multi-roles-project/fast/vue/src/views/login.vue` 迁移，更新标题为"AuraGate"。

- [ ] **Step 2: 迁移注册页**

从原 `register.vue` 迁移。

- [ ] **Step 3: 迁移系统管理页面**

从原项目迁移：
- `views/system/user/index.vue` — 用户管理列表页
- `views/system/role/index.vue` — 角色管理列表页
- `views/system/menu/index.vue` — 菜单管理树形页
- `views/system/user/profile.vue` — 个人信息页

### Task 5.4: 创建 AI 智能体对话页面

- [ ] **Step 1: 创建 agent.vue**

从原 `yu-ai-agent-master/yu-ai-agent-frontend/src/views/SuperAgent.vue` 和 `ChatRoom.vue` 的设计思路改造：

```vue
<template>
  <div class="ai-agent-page">
    <div class="chat-header">
      <h2>AuraAgent</h2>
      <span class="status">在线</span>
    </div>
    <div class="chat-messages" ref="messageContainer">
      <div v-for="msg in messages" :key="msg.id"
           :class="['message', msg.role]">
        <div class="avatar">
          <el-avatar :icon="msg.role === 'user' ? UserFilled : Promotion" />
        </div>
        <div class="content" v-html="renderMessage(msg.content)"></div>
      </div>
    </div>
    <div class="chat-input">
      <el-input v-model="inputText" type="textarea" :rows="3"
                placeholder="输入你的问题..." @keydown="handleKeydown" />
      <el-button type="primary" @click="sendMessage" :loading="isLoading">
        发送
      </el-button>
    </div>
  </div>
</template>
```

- [ ] **Step 2: 创建 WebSocket 工具文件**

`src/utils/websocket.js`：

```javascript
import { Client } from '@stomp/stompjs'
import { getToken } from './auth'

let client = null

export function connectWebSocket(onMessage) {
  const token = getToken()
  client = new Client({
    brokerURL: `ws://localhost:8080/ws?token=${token}`,
    onConnect: () => {
      client.subscribe('/user/queue/ai/response', message => {
        onMessage(JSON.parse(message.body))
      })
    }
  })
  client.activate()
}

export function sendAiMessage(content) {
  if (client?.connected) {
    client.publish({
      destination: '/app/ai/chat',
      body: JSON.stringify({ content })
    })
  }
}

export function disconnectWebSocket() {
  client?.deactivate()
}
```

- [ ] **Step 3: 创建 API 文件**

`src/api/ai.js`：
- `sendChatMessage(content)` — 发送 AI 对话（REST）
- `getTaskResult(taskId)` — 获取异步任务结果

### Task 5.5: 创建仪表盘页面

- [ ] **Step 1: 创建 dashboard.vue**

```vue
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card>
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
```

展示：用户数、角色数、菜单数、AI 调用次数等统计。

### Task 5.6: 创建知识库管理页面

- [ ] **Step 1: 创建 docs.vue**

- 文档列表（分页表格）：文件名、类型、大小、上传时间、操作
- 上传按钮：支持上传 TXT/PDF/Markdown
- 删除功能

- [ ] **Step 2: 创建 qa.vue**

- 类似 AI 聊天界面，但基于 RAG 知识库
- 用户在输入框提问 → 后端检索知识库 → LLM 生成回答（附引用来源）

---

## Phase 6: 统一启动与集成测试

### Task 6.1: 创建统一启动类

- [ ] **Step 1: 创建 AuraGateApplication.java**

在 `auragate-parent/src/main/java/com/auragate/` 下：

```java
@SpringBootApplication(scanBasePackages = {"com.auragate"})
@EnableConfigurationProperties
public class AuraGateApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuraGateApplication.class, args);
    }
}
```

- [ ] **Step 2: 创建主 application.yml**

```yaml
server:
  port: 8080

spring:
  profiles:
    active: rbac, ai
  application:
    name: AuraGate
```

- [ ] **Step 3: 确保包扫描覆盖所有模块**

在启动类所在模块的 pom.xml 中添加对 rbac 和 ai 的依赖，确保 Maven 编译时包含所有模块。

### Task 6.2: 数据库初始化

- [ ] **Step 1: 创建 auragate_rbac.sql**

从原 `fast.sql` 复制并修改：
- 数据库名改为 `auragate`
- 表结构不变（user, role, menu, user_role, role_menu）
- 种子数据：默认 admin/admin 账号

- [ ] **Step 2: 创建 ES 索引**

创建 `auragate_knowledge` 和 `auragate_operation_log` 索引的 mapping。

### Task 6.3: 启动验证

- [ ] **Step 1: 启动后端**

```bash
cd d:\alldesk\code\AuraGate\auragate-parent
mvn clean install -DskipTests
mvn spring-boot:run
```

验证：访问 http://localhost:8080/api/health 确认启动成功

- [ ] **Step 2: 启动前端**

```bash
cd d:\alldesk\code\AuraGate\auragate-frontend
npm run dev
```

验证：访问 http://localhost:80 看到登录页

- [ ] **Step 3: 端到端测试**

1. 注册/登录 → 获取 Token
2. 查看仪表盘 → 显示统计信息
3. 用户管理 → CRUD 操作
4. 角色管理 → CRUD 操作
5. 菜单管理 → CRUD 操作
6. AuraAgent 对话 → 发送消息，查看流式回复
7. 知识库文档 → 上传文档
8. 知识问答 → 基于文档内容提问

---

## 其他清理事项

### 清理原始项目目录

在所有代码迁移完成后：

- [ ] **Step 1: 删除原始目录**

```bash
rm -rf d:\alldesk\code\AuraGate\multi-roles-project
rm -rf d:\alldesk\code\AuraGate\yu-ai-agent-master
```

### 清理引用配置

- [ ] **Step 1: 删除 `.claude/settings.local.json`** 中的原始 MySQL 权限（已不需要）

- [ ] **Step 2: 删除 .idea 目录**（如需要可重新生成）

---

## 附录：完整改名对照表

### 包名映射

| 原始包名 | 新包名 |
|----------|--------|
| `com.fast` | `com.auragate.rbac` |
| `com.yupi.yuaiagent` | `com.auragate.ai` |
| `com.yupi.yuimagesearchmcpserver` | `com.auragate.mcp` |

### 类名映射

| 原始类名 | 新类名 | 模块 |
|----------|--------|------|
| `YuManus` | `AuraAgent` | ai |
| `YuAiAgentApplication` | (合并到 AuraGateApplication) | - |
| `SpringbootApplication` | (合并到 AuraGateApplication) | - |
| `fastConfig` | `AuraGateConfig` | rbac |
| `MyTokenTextSplitter` | `TokenTextSplitter` | ai |
| `MyKeywordEnricher` | `KeywordEnricher` | ai |
| `PgVectorVectorStoreConfig` | `VectorStoreConfig` | ai |
| `LoveAppDocumentLoader` | `DocumentLoader` | ai |

### 删除的文件清单

| 文件 | 原因 |
|------|------|
| `LoveApp.java` | 恋爱大师 |
| `LoveAppDocumentLoader.java` | 恋爱专用 |
| `LoveAppRagCloudAdvisorConfig.java` | 恋爱专用 |
| `LoveAppRagCustomAdvisorFactory.java` | 恋爱专用 |
| `LoveAppVectorStoreConfig.java` | 恋爱专用（重复） |
| `LoveAppContextualQueryAugmenterFactory.java` | 恋爱专用 |
| `FileBasedChatMemory.java` | 改用 Redis |
| `MyLoggerAdvisor.java` | 恋爱相关 |
| `ReReadingAdvisor.java` | 恋爱相关 |
| 恋爱知识库 3 个 markdown 文件 | 恋爱知识库 |
| `demo/` 包全部文件 | 示例代码 |
| `LoveMaster.vue` | 恋爱大师页面 |
| AppFooter.vue 中的品牌信息行 | 鱼皮品牌 |

### 数据库名变更

| 原库名 | 新库名 | 用途 |
|--------|--------|------|
| `fast` | `auragate` | MySQL RBAC 数据 |
| (新建) | `auragate_ai` | PostgreSQL AI 向量数据 |
