/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : ai_knowledge

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 17/07/2026 16:48:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_model_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_config`;
CREATE TABLE `ai_model_config`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置名称',
  `model_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型类型',
  `provider` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务商',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接口地址',
  `api_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'API Key',
  `model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `enabled` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否启用',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `create_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI模型配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_model_config
-- ----------------------------
INSERT INTO `ai_model_config` VALUES (1, 'DeepSeek聊天模型配置', '聊天模型', 'DeepSeek', 'https://api.deepseek.com', '', 'deepseek-chat', '是', '用于企业智能客服最终回答生成。', '2026-06-05 10:30:00', '2026-06-06 16:41:21');
INSERT INTO `ai_model_config` VALUES (2, '百炼向量模型配置', '向量模型', '阿里云百炼', 'https://dashscope.aliyuncs.com/compatible-mode/v1', '', 'text-embedding-v4', '是', '用于知识片段 Embedding 向量化。', '2026-06-05 10:32:00', '2026-06-06 16:40:56');

-- ----------------------------
-- Table structure for knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库名称',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '知识库描述',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  `create_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新时间',
  `chunk_size` int NULL DEFAULT 500 COMMENT '切片长度',
  `overlap_size` int NULL DEFAULT 80 COMMENT '重叠长度',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_knowledge_base_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '知识库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_base
-- ----------------------------
INSERT INTO `knowledge_base` VALUES (1, '产品手册知识库', 'PRODUCT_MANUAL', '保存产品功能、部署方式、版本说明等资料，用于回答产品相关问题。', '启用', '2026-06-04 18:20:00', '2026-07-17 10:03:46', 500, 80);
INSERT INTO `knowledge_base` VALUES (2, '售后服务知识库', 'AFTER_SALES', '保存退款、发票、工单、售后响应流程等资料，用于回答客服咨询问题。', '启用', '2026-06-04 18:22:00', '2026-07-17 10:03:44', 500, 80);
INSERT INTO `knowledge_base` VALUES (3, '公司制度知识库', 'COMPANY_POLICY', '保存考勤、报销、权限申请等企业内部制度资料，用于内部员工咨询。', '启用', '2026-06-04 18:24:00', '2026-07-17 10:03:42', 500, 80);

-- ----------------------------
-- Table structure for knowledge_document
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_document`;
CREATE TABLE `knowledge_document`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_base_id` int NULL DEFAULT NULL COMMENT '知识库ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档标题',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名称',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件地址',
  `file_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `file_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件大小',
  `parsed_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '解析文本',
  `parse_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '解析状态',
  `parse_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '解析时间',
  `vector_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '向量化状态',
  `create_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '知识文档表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_document
-- ----------------------------
INSERT INTO `knowledge_document` VALUES (1, 1, '产品部署手册', '产品部署手册.pdf', 'http://localhost:9090/files/download/产品部署手册.pdf', 'pdf', '6KB', '产品部署手册\n一、文档目的\n本文档用于说明企业版产品的部署方式、部署前准备、基础环境要求、服务配置流程、常见部署问题和验收标准。\n二、支持的部署方式\n企业版产品支持标准服务器部署、内网私有化部署和混合部署。客户可以根据数据安全要求、网络策略和模型调用方式选择合适方案。\n标准服务器部署适合客户已有统一运维平台，并希望对服务器、数据库和日志进行自主管理。\n内网私有化部署适合金融、政企、医疗等对数据安全要求较高的客户。该方式下系统不主动访问公网服务。\n混合部署适合业务数据保存在内网，但允许通过受控网关调用外部大模型服务的场景。\n三、服务器环境要求\n应用服务器建议配置为 4 核 CPU、8GB 内存和 100GB 磁盘。数据库可以使用 MySQL 8.0 或 PostgreSQL 15 及以上。\n如果使用 PGVector，需要安装 PostgreSQL 和 pgvector 扩展。数据库需要单独设置业务账号，不建议使用超级管理员账号连接业务系统。\n四、基础组件说明\n系统主要包含 Web 前端、Spring Boot 后端、数据库、文件目录和向量检索组件。\nWeb 前端负责登录、后台管理、知识库维护、文档上传和智能客服交互。\nSpring Boot 后端负责用户认证、业务接口、文件上传、文档管理、知识切片、向量检索和大模型调用。\n数据库保存用户、知识库、知识文档、知识片段、向量记录、问答记录和来源引用记录。\nPGVector 用于保存知识片段向量。用户提问时，系统会把问题转换成向量，再从 PGVector 中检索相似知识片段。\n五、部署前准备\n部署前需要确认服务器地址、数据库连接、文件保存目录、应用访问域名、HTTPS 策略、统一认证要求、大模型服务方式和日志保留周期。\n六、部署流程\n第一步初始化数据库，创建业务库并执行项目 SQL 脚本。\n第二步上传后端应用，确认数据库地址、账号、密码和文件访问地址正确。\n第三步启动 Spring Boot 应用，观察控制台日志，确认服务端口正常监听且数据库连接成功。\n第四步构建前端项目，配置后端接口地址并生成静态资源。\n第五步访问系统登录页，使用管理员账号登录后台，检查用户管理、知识库管理、文档管理等页面是否正常。\n七、验收标准\n管理员可以正常登录系统，知识库管理页面可以创建和查询知识库，知识文档管理页面可以上传文件并保存文件地址。\n文件下载地址可以正常访问，后端日志没有数据库连接异常，页面刷新后路由访问正常。\n八、常见问题\n页面可以打开但接口请求失败时，需要检查前端接口地址、后端服务状态和跨域配置。\n文件上传成功但无法下载时，需要检查 fileBaseUrl 配置和 files 目录是否存在对应文件。\nPGVector 无法使用时，需要确认 PostgreSQL 已安装 pgvector 扩展，并在数据库中启用扩展。', '已解析', '2026-06-05 07:52:03', '已向量化', '2026-06-04 18:30:00', '2026-07-17 11:14:22');
INSERT INTO `knowledge_document` VALUES (2, 2, '售后退款流程', '售后退款流程.docx', 'http://localhost:9090/files/download/售后退款流程.docx', 'docx', '3KB', '售后退款流程\n\n一、文档目的\n\n本文档用于规范客户退款、退订、售后咨询和工单处理流程。客服、售后、财务和运营人员需要按照本文档处理客户退款申请，确保退款处理及时、记录完整、责任清晰。\n\n二、适用范围\n\n本文档适用于客户购买产品后申请退款、客户因服务不可用申请退订、客户重复付款需要退回多付金额、客户对订单金额或发票合同存在疑问等场景。\n\n三、退款受理原则\n\n客服收到退款申请后，需要先核实订单是否真实存在，包括订单号、客户账号、付款时间、付款金额和购买产品。退款原因需要记录在工单中，常见原因包括误购买、重复付款、服务未开通、功能不符合预期、合同条款约定可以退款、售后协商退款。\n\n四、退款处理流程\n\n客户可以通过在线客服、电话、邮件或工单系统提交退款申请。客服需要引导客户提供客户账号、订单号、付款时间、付款金额、退款原因、收款账户信息和发票情况。\n\n客服收到申请后，需要在系统中创建售后工单。工单标题建议格式为：退款申请-客户名称-订单号。工单内容需要包含客户申请信息、订单核验结果和初步处理意见。\n\n订单核验时，需要确认订单是否已支付、服务是否已开通、是否超过退款期限、是否已经申请过退款、是否存在未结清费用、是否开具发票。\n\n退款审核由售后负责人或运营负责人处理。审核时需要关注退款原因是否合理、合同条款是否允许退款、客户是否已经使用服务、退款金额是否准确、是否需要扣除已使用服务费用。\n\n审核通过后，财务根据工单信息执行退款。财务需要记录退款流水号、退款时间和实际退款金额。退款提交后，客服需要通知客户退款已提交、预计到账时间、到账账户和后续联系渠道。\n\n五、退款时效\n\n原路退回一般 1 到 7 个工作日到账；银行转账一般 3 到 5 个工作日到账；对公退款一般 5 到 10 个工作日到账。客服回复客户时，应说明预计到账时间，不要承诺一定在某个自然日到账。\n\n六、发票处理\n\n如果客户已经开具纸质发票，需要退回原发票，财务确认收到后再执行退款。如果是电子发票，财务需要确认是否可以红冲。未开发票的订单可以直接进入退款审核流程。\n\n七、特殊场景\n\n客户重复付款时，客服需要核对两笔支付流水。如果确认重复付款，一般退回重复支付金额。客户已经使用服务时，需要根据合同条款判断是否支持部分退款。合同客户退款需要同时核验合同条款、付款凭证和服务交付记录。\n\n八、客服回复示例\n\n您好，已收到您的退款申请。我们会先核验订单状态、付款记录和发票情况。审核通过后，退款会按原支付方式或约定账户退回。一般到账时间为 1 到 7 个工作日，具体以银行或支付渠道处理时间为准。\n\n九、工单关闭标准\n\n满足退款审核已完成、财务已提交退款、客户已收到到账通知、工单中已记录退款金额退款时间和处理人等条件后，工单可以关闭。', '已解析', '2026-06-05 07:51:37', '已向量化', '2026-06-04 18:32:00', '2026-07-17 14:28:34');
INSERT INTO `knowledge_document` VALUES (3, 3, '员工报销制度', '员工报销制度.txt', 'http://localhost:9090/files/download/员工报销制度.txt', 'txt', '5KB', '员工报销制度\n\n一、制度目的\n\n为规范公司费用报销流程，加强费用管理，提高财务审核效率，确保员工报销真实、合规、及时，特制定本制度。本制度适用于公司所有正式员工、实习员工和经公司授权产生业务费用的外部合作人员。\n\n二、适用范围\n\n本制度适用于以下费用类型：\n\n- 差旅交通费。\n- 住宿费。\n- 市内交通费。\n- 业务招待费。\n- 办公用品采购费。\n- 培训费。\n- 通讯费。\n- 项目执行相关费用。\n\n不属于公司业务需要的个人消费，不得申请报销。\n\n三、报销基本原则\n\n1. 真实发生\n\n所有报销费用必须真实发生，并与公司业务相关。员工不得虚构费用、重复报销或使用他人票据报销。\n\n2. 票据合规\n\n报销必须提供合法有效票据。发票抬头、税号、金额、日期、项目名称等信息需要完整准确。\n\n3. 及时提交\n\n费用发生后，员工应在规定时间内提交报销申请。差旅费用应在返程后 7 个工作日内提交，其他费用应在费用发生后 15 个工作日内提交。\n\n4. 预算控制\n\n涉及部门预算的费用，需要先确认预算余额。超预算费用必须提前申请审批。\n\n四、报销材料要求\n\n1. 差旅交通费\n\n需要提供：\n\n- 火车票、机票行程单、汽车票或打车发票。\n- 出差审批单。\n- 出差事由说明。\n- 如有改签或退票，需要说明原因。\n\n2. 住宿费\n\n需要提供：\n\n- 酒店住宿发票。\n- 酒店水单或订单截图。\n- 入住人信息。\n- 入住日期和离店日期。\n\n住宿标准由公司根据城市等级制定。超出标准部分原则上由个人承担，特殊情况需要部门负责人审批。\n\n3. 市内交通费\n\n需要提供：\n\n- 打车发票或平台订单截图。\n- 出行起点、终点和事由。\n- 同一天多笔交通费用需要逐条说明。\n\n4. 业务招待费\n\n需要提供：\n\n- 餐饮发票。\n- 招待对象。\n- 招待事由。\n- 参与人员名单。\n- 部门负责人审批记录。\n\n业务招待费需要提前申请。未经审批的高额招待费用，财务有权退回。\n\n5. 办公用品采购费\n\n需要提供：\n\n- 采购发票。\n- 商品清单。\n- 采购用途。\n- 验收或领用记录。\n\n五、报销流程\n\n1. 员工提交申请\n\n员工在报销系统中填写报销单，上传发票和相关附件，并选择费用类型、费用归属部门和项目。\n\n2. 部门负责人审批\n\n部门负责人审核费用是否真实、合理，是否符合部门预算和业务需要。\n\n3. 财务初审\n\n财务人员审核票据是否合规，金额是否一致，附件是否完整，费用类型是否正确。\n\n4. 财务复核\n\n金额较大或涉及特殊费用的报销，需要财务负责人复核。\n\n5. 出纳付款\n\n审核通过后，出纳按照付款批次执行付款。报销款一般发放至员工工资卡或公司登记的收款账户。\n\n六、报销时间\n\n公司每周集中处理两次报销付款，原则上为每周二和周五。遇到节假日时，付款时间顺延。\n\n员工提交报销后，如果资料完整、审批及时，一般在 5 个工作日内完成付款。\n\n七、退回和补充说明\n\n财务审核发现以下情况时，可以退回报销申请：\n\n- 发票信息错误。\n- 缺少审批记录。\n- 缺少附件。\n- 金额填写错误。\n- 费用类型选择错误。\n- 费用说明不清晰。\n- 超出标准但未提供审批。\n\n员工收到退回通知后，需要补充材料或修改报销单，并重新提交审批。\n\n八、不予报销情形\n\n以下费用不予报销：\n\n- 与公司业务无关的个人消费。\n- 无有效票据的费用。\n- 已经报销过的重复费用。\n- 未经审批的超标准费用。\n- 因个人原因产生的罚款、滞纳金、违约金。\n- 虚假票据或不合规票据。\n\n九、违规处理\n\n员工存在虚假报销、重复报销、伪造票据等行为的，公司将视情节严重程度采取以下措施：\n\n- 退回报销款。\n- 通报批评。\n- 取消一定期限内的报销资格。\n- 解除劳动合同。\n- 涉嫌违法的，移交司法机关处理。\n\n十、常见问题\n\n1. 发票抬头写错怎么办？\n\n员工需要联系开票方重新开具发票。无法重新开具的，原则上不予报销。\n\n2. 出差回来超过 7 个工作日还能报销吗？\n\n可以提交，但需要说明延迟原因。财务和部门负责人会根据实际情况审核。\n\n3. 电子发票可以报销吗？\n\n可以。电子发票需要保证真实有效，不得重复提交。\n\n4. 多人一起出差，费用可以由一个人统一报销吗？\n\n可以，但报销单中需要说明同行人员、费用分摊情况和业务事由。\n\n十一、附则\n\n本制度由财务部负责解释。制度发布后，所有员工应按照本制度执行。公司可根据业务发展和管理要求对制度进行修订。', '已解析', '2026-07-17 14:25:16', '已向量化', '2026-06-04 18:34:00', '2026-07-17 14:28:25');

-- ----------------------------
-- Table structure for knowledge_segment
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_segment`;
CREATE TABLE `knowledge_segment`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_base_id` int NULL DEFAULT NULL COMMENT '知识库ID',
  `document_id` int NULL DEFAULT NULL COMMENT '文档ID',
  `segment_index` int NULL DEFAULT NULL COMMENT '片段序号',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '片段内容',
  `char_count` int NULL DEFAULT NULL COMMENT '字符数',
  `vector_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '向量化状态',
  `create_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_knowledge_segment_document_id`(`document_id` ASC) USING BTREE,
  INDEX `idx_knowledge_segment_base_id`(`knowledge_base_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '知识片段表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_segment
-- ----------------------------
INSERT INTO `knowledge_segment` VALUES (8, 1, 1, 1, '产品部署手册\n一、文档目的\n本文档用于说明企业版产品的部署方式、部署前准备、基础环境要求、服务配置流程、常见部署问题和验收标准。\n二、支持的部署方式\n企业版产品支持标准服务器部署、内网私有化部署和混合部署。客户可以根据数据安全要求、网络策略和模型调用方式选择合适方案。\n标准服务器部署适合客户已有统一运维平台，并希望对服务器、数据库和日志进行自主管理。\n内网私有化部署适合金融、政企、医疗等对数据安全要求较高的客户。该方式下系统不主动访问公网服务。\n混合部署适合业务数据保存在内网，但允许通过受控网关调用外部大模型服务的场景。\n三、服务器环境要求\n应用服务器建议配置为 4 核 CPU、8GB 内存和 100GB 磁盘。数据库可以使用 MySQL 8.0 或 PostgreSQL 15 及以上。\n如果使用 PGVector，需要安装 PostgreSQL 和 pgvector 扩展。数据库需要单独设置业务账号，不建议使用超级管理员账号连接业务系统。\n四、基础组件说明\n系统主要包含 Web 前端、Spring Boot 后端、数据库、文件目录和向量检索组件。\nWeb 前端负责登录、后台管理、知识', 500, '已向量化', '2026-06-05 10:16:57', '2026-07-17 11:14:21');
INSERT INTO `knowledge_segment` VALUES (9, 1, 1, 2, '接业务系统。\n四、基础组件说明\n系统主要包含 Web 前端、Spring Boot 后端、数据库、文件目录和向量检索组件。\nWeb 前端负责登录、后台管理、知识库维护、文档上传和智能客服交互。\nSpring Boot 后端负责用户认证、业务接口、文件上传、文档管理、知识切片、向量检索和大模型调用。\n数据库保存用户、知识库、知识文档、知识片段、向量记录、问答记录和来源引用记录。\nPGVector 用于保存知识片段向量。用户提问时，系统会把问题转换成向量，再从 PGVector 中检索相似知识片段。\n五、部署前准备\n部署前需要确认服务器地址、数据库连接、文件保存目录、应用访问域名、HTTPS 策略、统一认证要求、大模型服务方式和日志保留周期。\n六、部署流程\n第一步初始化数据库，创建业务库并执行项目 SQL 脚本。\n第二步上传后端应用，确认数据库地址、账号、密码和文件访问地址正确。\n第三步启动 Spring Boot 应用，观察控制台日志，确认服务端口正常监听且数据库连接成功。\n第四步构建前端项目，配置后端接口地址并生成静态资源。\n第五步访问系统登录页，使用管理员账号登录后台，检查用户管理、', 500, '已向量化', '2026-06-05 10:16:57', '2026-07-17 11:14:22');
INSERT INTO `knowledge_segment` VALUES (10, 1, 1, 3, '日志，确认服务端口正常监听且数据库连接成功。\n第四步构建前端项目，配置后端接口地址并生成静态资源。\n第五步访问系统登录页，使用管理员账号登录后台，检查用户管理、知识库管理、文档管理等页面是否正常。\n七、验收标准\n管理员可以正常登录系统，知识库管理页面可以创建和查询知识库，知识文档管理页面可以上传文件并保存文件地址。\n文件下载地址可以正常访问，后端日志没有数据库连接异常，页面刷新后路由访问正常。\n八、常见问题\n页面可以打开但接口请求失败时，需要检查前端接口地址、后端服务状态和跨域配置。\n文件上传成功但无法下载时，需要检查 fileBaseUrl 配置和 files 目录是否存在对应文件。\nPGVector 无法使用时，需要确认 PostgreSQL 已安装 pgvector 扩展，并在数据库中启用扩展。', 355, '已向量化', '2026-06-05 10:16:57', '2026-07-17 11:14:22');
INSERT INTO `knowledge_segment` VALUES (27, 3, 3, 1, '员工报销制度\n\n一、制度目的\n\n为规范公司费用报销流程，加强费用管理，提高财务审核效率，确保员工报销真实、合规、及时，特制定本制度。本制度适用于公司所有正式员工、实习员工和经公司授权产生业务费用的外部合作人员。\n\n二、适用范围\n\n本制度适用于以下费用类型：\n\n- 差旅交通费。\n- 住宿费。\n- 市内交通费。\n- 业务招待费。\n- 办公用品采购费。\n- 培训费。\n- 通讯费。\n- 项目执行相关费用。\n\n不属于公司业务需要的个人消费，不得申请报销。\n\n三、报销基本原则\n\n1. 真实发生\n\n所有报销费用必须真实发生，并与公司业务相关。员工不得虚构费用、重复报销或使用他人票据报销。\n\n2. 票据合规\n\n报销必须提供合法有效票据。发票抬头、税号、金额、日期、项目名称等信息需要完整准确。\n\n3. 及时提交\n\n费用发生后，员工应在规定时间内提交报销申请。差旅费用应在返程后 7 个工作日内提交，其他费用应在费用发生后 15 个工作日内提交。\n\n4. 预算控制\n\n涉及部门预算的费用，需要先确认预算余额。超预算费用必须提前申请审批。\n\n四、报销材料要求\n\n1. 差旅交通费\n\n需要提供：', 490, '已向量化', '2026-07-17 14:25:49', '2026-07-17 14:28:07');
INSERT INTO `knowledge_segment` VALUES (28, 3, 3, 2, '- 火车票、机票行程单、汽车票或打车发票。\n- 出差审批单。\n- 出差事由说明。\n- 如有改签或退票，需要说明原因。\n\n2. 住宿费\n\n需要提供：\n\n- 酒店住宿发票。\n- 酒店水单或订单截图。\n- 入住人信息。\n- 入住日期和离店日期。\n\n住宿标准由公司根据城市等级制定。超出标准部分原则上由个人承担，特殊情况需要部门负责人审批。\n\n3. 市内交通费\n\n需要提供：\n\n- 打车发票或平台订单截图。\n- 出行起点、终点和事由。\n- 同一天多笔交通费用需要逐条说明。\n\n4. 业务招待费\n\n需要提供：\n\n- 餐饮发票。\n- 招待对象。\n- 招待事由。\n- 参与人员名单。\n- 部门负责人审批记录。\n\n业务招待费需要提前申请。未经审批的高额招待费用，财务有权退回。\n\n5. 办公用品采购费\n\n需要提供：\n\n- 采购发票。\n- 商品清单。\n- 采购用途。\n- 验收或领用记录。\n\n五、报销流程\n\n1. 员工提交申请\n\n员工在报销系统中填写报销单，上传发票和相关附件，并选择费用类型、费用归属部门和项目。\n\n2. 部门负责人审批\n\n部门负责人审核费用是否真实、合理，是否符合部门预算和业务需要。', 493, '已向量化', '2026-07-17 14:25:49', '2026-07-17 14:28:10');
INSERT INTO `knowledge_segment` VALUES (29, 3, 3, 3, '3. 财务初审\n\n财务人员审核票据是否合规，金额是否一致，附件是否完整，费用类型是否正确。\n\n4. 财务复核\n\n金额较大或涉及特殊费用的报销，需要财务负责人复核。\n\n5. 出纳付款\n\n审核通过后，出纳按照付款批次执行付款。报销款一般发放至员工工资卡或公司登记的收款账户。\n\n六、报销时间\n\n公司每周集中处理两次报销付款，原则上为每周二和周五。遇到节假日时，付款时间顺延。\n\n员工提交报销后，如果资料完整、审批及时，一般在 5 个工作日内完成付款。\n\n七、退回和补充说明\n\n财务审核发现以下情况时，可以退回报销申请：\n\n- 发票信息错误。\n- 缺少审批记录。\n- 缺少附件。\n- 金额填写错误。\n- 费用类型选择错误。\n- 费用说明不清晰。\n- 超出标准但未提供审批。\n\n员工收到退回通知后，需要补充材料或修改报销单，并重新提交审批。\n\n八、不予报销情形\n\n以下费用不予报销：\n\n- 与公司业务无关的个人消费。\n- 无有效票据的费用。\n- 已经报销过的重复费用。\n- 未经审批的超标准费用。\n- 因个人原因产生的罚款、滞纳金、违约金。\n- 虚假票据或不合规票据。\n\n九、违规处理', 488, '已向量化', '2026-07-17 14:25:49', '2026-07-17 14:28:23');
INSERT INTO `knowledge_segment` VALUES (30, 3, 3, 4, '员工存在虚假报销、重复报销、伪造票据等行为的，公司将视情节严重程度采取以下措施：\n\n- 退回报销款。\n- 通报批评。\n- 取消一定期限内的报销资格。\n- 解除劳动合同。\n- 涉嫌违法的，移交司法机关处理。\n\n十、常见问题\n\n1. 发票抬头写错怎么办？\n\n员工需要联系开票方重新开具发票。无法重新开具的，原则上不予报销。\n\n2. 出差回来超过 7 个工作日还能报销吗？\n\n可以提交，但需要说明延迟原因。财务和部门负责人会根据实际情况审核。\n\n3. 电子发票可以报销吗？\n\n可以。电子发票需要保证真实有效，不得重复提交。\n\n4. 多人一起出差，费用可以由一个人统一报销吗？\n\n可以，但报销单中需要说明同行人员、费用分摊情况和业务事由。\n\n十一、附则\n\n本制度由财务部负责解释。制度发布后，所有员工应按照本制度执行。公司可根据业务发展和管理要求对制度进行修订。', 377, '已向量化', '2026-07-17 14:25:49', '2026-07-17 14:28:25');
INSERT INTO `knowledge_segment` VALUES (31, 2, 2, 1, '售后退款流程\n\n一、文档目的\n\n本文档用于规范客户退款、退订、售后咨询和工单处理流程。客服、售后、财务和运营人员需要按照本文档处理客户退款申请，确保退款处理及时、记录完整、责任清晰。\n\n二、适用范围\n\n本文档适用于客户购买产品后申请退款、客户因服务不可用申请退订、客户重复付款需要退回多付金额、客户对订单金额或发票合同存在疑问等场景。\n\n三、退款受理原则\n\n客服收到退款申请后，需要先核实订单是否真实存在，包括订单号、客户账号、付款时间、付款金额和购买产品。退款原因需要记录在工单中，常见原因包括误购买、重复付款、服务未开通、功能不符合预期、合同条款约定可以退款、售后协商退款。\n\n四、退款处理流程\n\n客户可以通过在线客服、电话、邮件或工单系统提交退款申请。客服需要引导客户提供客户账号、订单号、付款时间、付款金额、退款原因、收款账户信息和发票情况。\n\n客服收到申请后，需要在系统中创建售后工单。工单标题建议格式为：退款申请-客户名称-订单号。工单内容需要包含客户申请信息、订单核验结果和初步处理意见。', 451, '已向量化', '2026-07-17 14:27:37', '2026-07-17 14:28:33');
INSERT INTO `knowledge_segment` VALUES (32, 2, 2, 2, '订单核验时，需要确认订单是否已支付、服务是否已开通、是否超过退款期限、是否已经申请过退款、是否存在未结清费用、是否开具发票。\n\n退款审核由售后负责人或运营负责人处理。审核时需要关注退款原因是否合理、合同条款是否允许退款、客户是否已经使用服务、退款金额是否准确、是否需要扣除已使用服务费用。\n\n审核通过后，财务根据工单信息执行退款。财务需要记录退款流水号、退款时间和实际退款金额。退款提交后，客服需要通知客户退款已提交、预计到账时间、到账账户和后续联系渠道。\n\n五、退款时效\n\n原路退回一般 1 到 7 个工作日到账；银行转账一般 3 到 5 个工作日到账；对公退款一般 5 到 10 个工作日到账。客服回复客户时，应说明预计到账时间，不要承诺一定在某个自然日到账。\n\n六、发票处理\n\n如果客户已经开具纸质发票，需要退回原发票，财务确认收到后再执行退款。如果是电子发票，财务需要确认是否可以红冲。未开发票的订单可以直接进入退款审核流程。\n\n七、特殊场景', 425, '已向量化', '2026-07-17 14:27:37', '2026-07-17 14:28:34');
INSERT INTO `knowledge_segment` VALUES (33, 2, 2, 3, '客户重复付款时，客服需要核对两笔支付流水。如果确认重复付款，一般退回重复支付金额。客户已经使用服务时，需要根据合同条款判断是否支持部分退款。合同客户退款需要同时核验合同条款、付款凭证和服务交付记录。\n\n八、客服回复示例\n\n您好，已收到您的退款申请。我们会先核验订单状态、付款记录和发票情况。审核通过后，退款会按原支付方式或约定账户退回。一般到账时间为 1 到 7 个工作日，具体以银行或支付渠道处理时间为准。\n\n九、工单关闭标准\n\n满足退款审核已完成、财务已提交退款、客户已收到到账通知、工单中已记录退款金额退款时间和处理人等条件后，工单可以关闭。', 274, '已向量化', '2026-07-17 14:27:37', '2026-07-17 14:28:34');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 'admin', '管理员', 'http://localhost:9090/files/download/avatar.jpeg', 'ADMIN', '18899990011', 'admin@163.com');
INSERT INTO `user` VALUES (2, 'aaa', '123', '张三', 'http://localhost:9090/files/download/1775913605328-8.png', 'USER', '18800009999', 'zs@163.com');
INSERT INTO `user` VALUES (3, 'bbb', '123', '李四', 'http://localhost:9090/files/download/1775915092599-17.png', 'USER', '18877776666', 'ls@163.com');

SET FOREIGN_KEY_CHECKS = 1;
