# AI 企业知识库 · 数据库脚本导入说明

本项目用到 **两个数据库**，导入数据时两个都要处理，项目才能正常运行：

| 数据库 | 存什么 | 对应脚本 |
| --- | --- | --- |
| **MySQL** | 业务数据（用户、知识库、文档、片段、模型配置等） | `ai_knowledge.sql` |
| **PostgreSQL + PGVector** | 向量数据（知识片段的 Embedding 向量） | `ai_knowledge_vector.sql` |

> 简单说：MySQL 存“文字和业务”，PostgreSQL 存“向量”。两个脚本分别导入到对应的数据库里。

---

## 一、准备工作（先装好这些软件）

1. **安装 MySQL**（建议 8.x），默认端口 `3306`。
2. **安装 PostgreSQL + pgvector 扩展**，默认端口 `5432`。
   - 详细安装步骤见喂饭教程文档 **《09-Windows 本地安装 PostgreSQL 与 PGVector》**，一步步跟着装即可。
   - ⚠️ 一定要装 **pgvector 扩展**，否则向量脚本会报 `type "vector" does not exist`。
3. **安装数据库管理工具**：
   - **Navicat for MySQL** —— 用来连 MySQL。
   - **Navicat for PostgreSQL** —— 用来连 PostgreSQL。
   - 如果你装的是 **Navicat Premium**，一个软件就够了（它同时支持 MySQL 和 PostgreSQL）。
   - ⚠️ **Navicat for MySQL 连不了 PostgreSQL**，反之亦然，别用错工具。

---

## 二、导入 MySQL 业务数据（`ai_knowledge.sql`）

1. 打开 **Navicat for MySQL**，新建连接，连上本地 MySQL：
   - 主机 `localhost`、端口 `3306`、用户名 `root`、密码填你本地 MySQL 的密码（仓库 `application.yml` 里写的默认值是 `Root@12345`，**以你本机实际密码为准**）。
2. 在连接上**新建数据库**：
   - 数据库名：`ai_knowledge`
   - 字符集：`utf8mb4`
   - 排序规则：`utf8mb4_general_ci`
3. **双击进入 `ai_knowledge` 数据库** → 右键 → **运行 SQL 文件…** → 选择 `ai_knowledge.sql` → 开始。
4. 刷新左侧，能看到 `user`、`knowledge_base`、`knowledge_document`、`knowledge_segment`、`ai_model_config` 等表，就说明导入成功。

---

## 三、导入 PostgreSQL 向量数据（`ai_knowledge_vector.sql`）

> 前提：第一步里的 **PostgreSQL 和 pgvector 扩展已经装好**（见 09 文档）。

1. 打开 **Navicat for PostgreSQL**，新建连接，连上本地 PostgreSQL：
   - 主机 `localhost`、端口 `5432`、用户名 `postgres`、密码 `123456`（或你安装 PostgreSQL 时设置的密码）。
2. 新建数据库：
   - 数据库名：`ai_knowledge_vector`
3. **双击进入 `ai_knowledge_vector` 数据库** → 右键 → **运行 SQL 文件…** → 选择 `ai_knowledge_vector.sql` → 开始。
   - 这个脚本已经包含了 `CREATE EXTENSION vector` + 建表 + 向量数据 + 索引，**可以直接执行、也可以重复执行**（重复执行会先清空旧表再重建）。
4. 刷新左侧，能看到 `knowledge_segment_vector` 表、里面有向量数据，就说明导入成功。

---

## 四、核对项目配置（`application.yml`）

项目后端配置文件在：

```text
ai_knowledge/springboot/src/main/resources/application.yml
```

默认配置如下，**如果你本地的端口 / 账号 / 密码和下面不一样，就改这里对应的值**：

```yaml
# MySQL 业务库（口令支持环境变量覆盖：export MYSQL_PASSWORD=你的密码）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_knowledge?...
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:Root@12345}

# PostgreSQL 向量库（口令支持环境变量覆盖：export PG_PASSWORD=你的密码）
pgvector:
  url: jdbc:postgresql://localhost:5432/ai_knowledge_vector
  username: ${PG_USERNAME:postgres}
  password: ${PG_PASSWORD:123456}
  tableName: knowledge_segment_vector
  dimension: 1024

# 上传文件目录：相对「后端进程工作目录」解析，
# 后端需在 ai_knowledge/ 目录下启动，否则读不到仓库自带的 files/
file:
  base-path: ./files/
```

> 上面的口令都只是**本机开发的默认值**，以你自己的实际密码为准；部署到生产请用环境变量注入，不要把明文口令提交到公开仓库。

---

## 五、常见问题

- **执行向量脚本报 `type "vector" does not exist`**
  → PostgreSQL 没装 pgvector 扩展，回看 09 文档把扩展装上。

- **Navicat for MySQL 打不开 PostgreSQL 的库**
  → 正常现象，它只支持 MySQL。PostgreSQL 要用 **Navicat for PostgreSQL** 或 **Navicat Premium**。

- **中文乱码**
  → MySQL 数据库字符集要用 `utf8mb4`；导入时确保脚本文件是 UTF-8 编码。

- **向量脚本执行时提示表已存在 / 冲突**
  → 本脚本已带 `DROP TABLE IF EXISTS`，可以重复执行；如果仍报错，确认你连的是 `ai_knowledge_vector` 这个库。

- **不想导入向量数据**
  → 也可以不导入 `ai_knowledge_vector.sql`，自己在管理端页面对知识片段执行「批量向量化」重新生成向量（前提是在“模型配置”里配好了可用的向量模型和真实 API Key）。

---

## 六、整体启动顺序

```text
1. 装好 MySQL、PostgreSQL（含 pgvector 扩展）
2. 导入 ai_knowledge.sql          → MySQL 的 ai_knowledge 库
3. 导入 ai_knowledge_vector.sql   → PostgreSQL 的 ai_knowledge_vector 库
4. 按需修改 application.yml 的账号 / 密码 / 端口
5. 启动后端（Spring Boot）
6. 启动前端（Vue）
```

到这里数据库就准备好了，项目可以正常跑起来。
