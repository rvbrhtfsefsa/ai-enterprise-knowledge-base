# AI 企业知识库

一套开箱即用的**企业知识库 + 智能问答（RAG）**系统。把企业内部文档（Word / PDF / TXT）上传后，系统自动解析成分段知识、生成向量并存入向量数据库；用户提问时通过向量召回相关片段，再交给大模型生成带引用来源的流式回答。

前端采用 Spring Boot 3 + Vue 3 前后端分离架构，向量模型与聊天模型均可在管理后台可视化配置（OpenAI 兼容接口），无需改动代码即可切换供应商。

---

## 一、主要功能

| 模块 | 能力说明 |
| --- | --- |
| 用户与权限 | 注册 / 登录 / 修改密码 / 个人资料；JWT 鉴权；角色区分 `ADMIN` / `USER`，普通用户无法进入管理端 |
| 知识库管理 | 知识库的增删改查、批量删除、分页查询 |
| 知识文档管理 | 上传 `.docx` / `.pdf` / `.txt` 文档，一键解析正文，支持按知识库归类 |
| 知识片段管理 | 由文档自动生成知识片段（segment），支持单条 / 批量向量化 |
| 模型配置 | 可视化维护**向量模型（Embedding）与聊天模型（Chat）**的 `baseUrl` / `apiKey` / `modelName`，可随时启用停用 |
| 管理端智能问答 | SSE 流式输出回答，展示命中的知识片段来源 |
| 前台智能客服 | 面向普通用户的问答页面，同样支持流式回答 |
| 文件服务 | 头像 / 文档上传与下载 |

> 核心 RAG 链路：文档解析 → 片段切分 → Embedding 向量化 → 存入 pgvector → 问题向量化 → 相似度召回（默认 topK=5，阈值 0.2）→ 拼接上下文 → 大模型流式生成答案。

### 内置测试账号（来自 `ai_knowledge.sql`）

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin` | ADMIN（管理端） |
| `aaa` | `123` | USER（前台） |
| `bbb` | `123` | USER（前台） |

---

## 二、技术栈

**后端**
- Java 21、Spring Boot 3.3.1
- MyBatis 3.0.3（XML 映射 + `@MapperScan`）
- MySQL 8.x（业务数据）+ PostgreSQL + pgvector（向量数据）
- JWT（`com.auth0:java-jwt` 4.3.0）、Hutool 5.8.25、PageHelper 1.4.6
- Apache POI 5.2.5（解析 Word）、PDFBox 2.0.30（解析 PDF）
- Maven 构建，默认端口 `9090`

**前端**
- Vue 3.4 + Vite 5 + Vue Router 4
- Element Plus 2.8（按需自动导入 + SCSS 主题定制）
- Axios（统一拦截器，超时 120s）、sass 1.71.1
- 默认开发端口 `5173`

---

## 三、环境要求

| 依赖 | 版本要求 | 本项目验证版本 |
| --- | --- | --- |
| JDK | 21 及以上 | OpenJDK 21.0.12 |
| Maven | 3.6+ | 3.9.16 |
| Node.js | 18+（推荐 20 LTS / 22 LTS），已在 `package.json` 的 `engines` 中声明 | v22.22.2 |
| MySQL | 8.x | 8.0.40 |
| PostgreSQL + pgvector | 14+ / 0.5+ | PostgreSQL 18.4 |
| 大模型服务 | 任一 OpenAI 兼容接口（任选其一：通义千问 / 智谱 / DeepSeek / OpenAI 等） | — |

> 需要**两个数据库**：MySQL 存业务数据，PostgreSQL 存向量数据，二者缺一不可。
> Windows 下安装 PostgreSQL 与 pgvector 的图文步骤见《软件安装详细文档.docx》中的「09-Windows 本地安装 PostgreSQL 与 PGVector」。

---

## 四、安装与启动

### 1. 初始化数据库

```text
① MySQL      新建库 ai_knowledge（utf8mb4）         → 执行 ai_knowledge.sql
② PostgreSQL 新建库 ai_knowledge_vector             → 执行 ai_knowledge_vector.sql
```

`ai_knowledge.sql` 含 `user`、`knowledge_base`、`knowledge_document`、`knowledge_segment`、`ai_model_config` 五张表；`ai_knowledge_vector.sql` 含 `CREATE EXTENSION vector` + `knowledge_segment_vector` 表 + 索引，可重复执行。

### 2. 修改后端配置

编辑 `ai_knowledge/springboot/src/main/resources/application.yml`，把账号密码改成你本机的实际值。仓库里的口令都写了 `${环境变量:默认值}` 的形式，**既可以直接改默认值，也可以不动文件、改用环境变量注入**：

```yaml
spring:
  datasource:
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:Root@12345}     # 或 export MYSQL_PASSWORD=你的密码
    url: jdbc:mysql://localhost:3306/ai_knowledge?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true

# 上传文件存放目录（见下方 ⚠️ 说明）
file:
  base-path: ./files/

pgvector:
  url: jdbc:postgresql://localhost:5432/ai_knowledge_vector
  username: ${PG_USERNAME:postgres}
  password: ${PG_PASSWORD:123456}              # 或 export PG_PASSWORD=你的密码
  tableName: knowledge_segment_vector
  dimension: 1024      # 必须与你的 Embedding 模型维度一致
```

> ⚠️ **`file.base-path` 很关键**：它是相对「后端进程工作目录」解析的，而仓库自带的示例文件（头像、示例文档）在 `ai_knowledge/files/`。
> 所以后端必须在 **`ai_knowledge/`** 这一层启动，默认配置 `./files/` 才能命中它们。
> 如果你想从任意目录启动，把它改成绝对路径即可，例如 `file.base-path: D:/data/ai-knowledge-files/`（改完记得把仓库里的示例文件复制过去）。

> 安全提示：`application.yml` 中的口令仅为**本地开发的默认值**。**请勿把生产环境账号密码提交到公开仓库**，生产部署请一律使用环境变量注入。另外注意：本仓库 `application.yml` 里 MySQL 默认是 `Root@12345`，与《SQL脚本说明文档.md》中提到的 `123456` 只是示例值，**两者都以你本机实际密码为准**。

### 3. 启动后端

```bash
cd ai_knowledge                                     # ⚠️ 必须在这一层，不要在 springboot/ 下启动
mvn -f springboot/pom.xml clean package -DskipTests # 构建
mvn -f springboot/pom.xml spring-boot:run           # 运行
# 或者：
java -jar springboot/target/springboot-0.0.1-SNAPSHOT.jar
# IDEA 用户：把运行配置 Working directory 设为 <仓库根>/ai_knowledge，再运行 com.example.SpringbootApplication
```

> ⚠️ **为什么不能 `cd ai_knowledge/springboot` 后启动？**
> 上传/下载目录默认是相对工作目录的 `./files/`。从 `springboot/` 启动会去找 `springboot/files/`（不存在），
> 表现是：接口返回 200 但内容为空、头像全部破图、文档解析报「本地文件不存在」，且不抛明显错误，很难排查。
> 若确实要在 `springboot/` 下启动，请把 `file.base-path` 改成绝对路径。

### 4. 启动前端

```bash
cd ai_knowledge/vue
npm install
npm run dev
# 浏览器打开 http://localhost:5173
```

### 5. 配置模型（首次使用必做）

登录管理端 → **模型配置** → 新增两条记录：

| 字段 | 向量模型示例 | 聊天模型示例 |
| --- | --- | --- |
| 模型类型 | embedding | chat |
| 服务商 | 阿里云百炼 / 智谱 / 随便填 | 同左 |
| 接口地址 baseUrl | `https://dashscope.aliyuncs.com/compatible-mode/v1` | `https://dashscope.aliyuncs.com/compatible-mode/v1` |
| API Key | `sk-xxxxxx` | `sk-xxxxxx` |
| 模型名称 | `text-embedding-v3`（1024 维） | `qwen-plus` |
| 启用 | 是 | 是 |

### 6. 生产打包

```bash
# 后端
cd ai_knowledge/springboot && mvn clean package -DskipTests
java -jar target/springboot-0.0.1-SNAPSHOT.jar

# 前端（先在 .env.production 中把 VITE_BASE_URL 改成真实后端地址）
cd ai_knowledge/vue && npm run build     # 产物在 vue/dist，交给 Nginx 托管即可
```

**整体启动顺序**：装好 MySQL / PostgreSQL(pgvector) → 导入两个 SQL → 改 `application.yml` → 在 `ai_knowledge/` 层启动后端（9090）→ 启动前端（5173）→ 配模型 → 上传文档 → 片段向量化 → 开始问答。

> 想改端口：改 `application.yml` 的 `server.port`（后端，默认 9090）；前端改 `package.json` 的 `dev` 脚本或 `vite.config.js` 的 `server.port`（默认 5173）。改端口后记得同步 `.env.development` 的 `VITE_BASE_URL`。

---

## 五、目录结构

```text
AIGC编程网-AI企业知识库/
├── README.md                        # 本文件
├── LICENSE                          # MIT 许可证（如需更换请自行替换）
├── 全新环境可跑通性检查报告.md        # 全新克隆环境的验证记录（可删）
├── .gitignore                       # Git 忽略规则（依赖/构建产物/密钥/上传目录）
├── .gitattributes                   # 换行符与二进制文件处理规则
├── ai_knowledge.sql                 # MySQL 业务库脚本（5 张表 + 初始数据）
├── ai_knowledge_vector.sql          # PostgreSQL + pgvector 向量库脚本
├── SQL脚本说明文档.md                # 数据库导入图文说明
├── 软件安装详细文档.docx              # 环境安装喂饭教程（含 PostgreSQL/pgvector）
├── springboot3+vue3系统运行方式.docx  # 前后端运行方式说明
├── ai_knowledge/
│   ├── files/                       # 运行时上传目录（头像、示例文档），仓库仅保留样例文件
│   ├── springboot/                  # ── 后端工程 ──
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/com/example/
│   │       │   ├── SpringbootApplication.java        # 启动入口（@MapperScan + @EnableAsync）
│   │       │   ├── common/                           # Result / ResultCodeEnum / Constants
│   │       │   │   └── config/                       # CorsConfig / WebConfig / JWTInterceptor
│   │       │   ├── config/PgVectorConfig.java        # 读取 pgvector.* 配置
│   │       │   ├── controller/                       # user / knowledgeBase / knowledgeDocument /
│   │       │   │                                     # knowledgeSegment / knowledgeChat / aiModelConfig / files
│   │       │   ├── entity/  mapper/  service/  utils/ # 实体 / MyBatis Mapper / 业务层 / Token 工具
│   │       │   └── exception/                        # 自定义异常 + 全局异常处理
│   │       └── resources/
│   │           ├── application.yml                   # 数据源、端口、pgvector 配置
│   │           └── mapper/*.xml                      # MyBatis SQL 映射
│   └── vue/                         # ── 前端工程 ──
│       ├── index.html               # 页面模板（title: AI企业知识库）
│       ├── vite.config.js           # Vite 配置（别名 @、Element Plus 按需导入、预加载依赖）
│       ├── package.json             # 依赖与 dev/build/preview 脚本
│       ├── .env.development / .env.production        # 后端接口地址
│       └── src/
│           ├── main.js              # 入口（挂载 router + Element Plus + 中文语言包）
│           ├── App.vue
│           ├── router/index.js      # 路由 + 管理端 ADMIN 权限守卫
│           ├── utils/request.js     # Axios 封装（自动携带 token、统一错误提示）
│           ├── assets/              # css（global/front/manager/index.scss）、imgs
│           └── views/
│               ├── Login.vue / Register.vue / 404.vue
│                                      ├── Manager.vue + manager/     # 管理端：用户、知识库、文档、片段、模型配置、问答
│               └── Front.vue  + front/        # 前台：智能客服、个人资料、修改密码
```

> `extracted/` 是历史解压残留的重复旧副本，已删除；如需找回见 `.workbuddy/backups/extracted-backup-2026-09-27.zip` 或 `git show HEAD~1`。

---

## 六、基本使用示例

### 页面路径

| 路径 | 说明 |
| --- | --- |
| `/login` | 登录 |
| `/register` | 注册 |
| `/manager/home` | 管理端首页（需 ADMIN） |
| `/manager/knowledgeBase` | 知识库管理 |
| `/manager/knowledgeDocument` | 知识文档上传与解析 |
| `/manager/knowledgeSegment` | 片段生成与向量化 |
| `/manager/aiModelConfig` | 模型配置 |
| `/manager/knowledgeChat` | 管理端问答 |
| `/front/FrontHome` | 前台智能客服 |

### 完整走通一次问答

```text
1. 管理端 → 知识库管理        → 新增「产品知识库」
2. 管理端 → 知识文档管理      → 上传《售后退款流程.docx》，选好所属知识库 → 点「解析」
3. 管理端 → 知识片段管理      → 点「生成片段」→ 全选 → 点「批量向量化」
4. 管理端 → 智能客服问答      → 选择聊天模型 + 向量模型 → 输入「退款要多久？」→ 流式返回答案与来源片段
5. 前台  → 智能客服          → 普通用户直接提问
```

### 接口示例

```bash
# 登录（返回 token，后续请求放 Header: token）
curl -X POST http://localhost:9090/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# 分页查询知识文档（token 换成上一步返回值）
curl "http://localhost:9090/knowledgeDocument/selectPage?pageNum=1&pageSize=10" \
  -H "token: <token>"

# 对某个文档生成知识片段
curl -X POST http://localhost:9090/knowledgeSegment/generate/1 -H "token: <token>"

# 批量向量化片段
curl -X POST http://localhost:9090/knowledgeSegment/vectorize/batch \
  -H "Content-Type: application/json" -H "token: <token>" \
  -d '{"modelConfigId":2,"ids":[1,2,3]}'

# SSE 流式问答
curl -N -X POST http://localhost:9090/knowledgeChat/askStream \
  -H "Content-Type: application/json" -H "token: <token>" \
  -d '{"question":"退款流程是怎样的？","chatModelConfigId":1,"embeddingModelConfigId":2,"topK":5,"similarityThreshold":0.2}'
```

主要接口前缀：`/user`、`/knowledgeBase`、`/knowledgeDocument`、`/knowledgeSegment`、`/knowledgeChat`、`/aiModelConfig`、`/files`。

---

## 七、常见问题

- **`type "vector" does not exist`** → PostgreSQL 没装 pgvector 扩展，参见《软件安装详细文档.docx》第 09 章。
- **头像全空白 / 文档解析报「本地文件不存在」** → 后端启动目录不对。请 `cd ai_knowledge` 再启动，或把 `application.yml` 的 `file.base-path` 改成绝对路径（见第 2、3 步的 ⚠️）。
- **端口被占用或想换端口** → 改 `server.port`；若启动时发现端口变成了别的数字（如 49247），检查环境变量里是否有 `SERVER_PORT` / `SERVER__PORT`，Spring Boot 会用它覆盖配置。
- **Windows 控制台中文日志乱码** → 启动时加 `-Dfile.encoding=UTF-8`（仅影响日志显示，不影响功能）。
- **向量问答返回空** → 片段尚未向量化，或 Embedding 模型维度与 `application.yml` 里 `dimension` 不一致。
- **接口 404 / 500** → 确认后端已在 9090 启动，且前端 `.env` 中的 `VITE_BASE_URL` 指向正确地址。
- **普通用户点管理端菜单没反应** → 正常行为，`/manager` 只允许 ADMIN 访问，会被重定向到前台首页。
- **Node 版本过低导致 Vite 报错** → 升级到 Node 18 及以上。

---

## 八、许可

本项目为 AIGC 编程网教学/演示项目，源码与 SQL 脚本随项目分发。使用时请自行保管 OpenAI / 大模型平台的 API Key，不要将其提交到版本库。
