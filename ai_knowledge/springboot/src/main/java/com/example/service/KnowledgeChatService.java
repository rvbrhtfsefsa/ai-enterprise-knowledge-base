package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.AiModelConfig;
import com.example.entity.KnowledgeDocument;
import com.example.entity.KnowledgeChatRequest;
import com.example.entity.KnowledgeSource;
import com.example.exception.CustomException;
import com.example.mapper.AiModelConfigMapper;
import com.example.mapper.KnowledgeDocumentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
public class KnowledgeChatService {

    private static final int DEFAULT_TOP_K = 5;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.2;

    @Resource
    private AiModelConfigMapper aiModelConfigMapper;
    @Resource
    private EmbeddingService embeddingService;
    @Resource
    private PgVectorService pgVectorService;
    @Resource
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    /**
     * 管理端流式问答：校验模型和参数后走 SSE 流式返回
     */
    public SseEmitter chatStream(KnowledgeChatRequest request) {
        if (request == null || StrUtil.isBlank(request.getQuestion())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请输入问题");
        }
        AiModelConfig embeddingModel = getModelConfig(request.getEmbeddingModelConfigId(), "向量模型");
        AiModelConfig chatModel = getModelConfig(request.getChatModelConfigId(), "聊天模型");
        Integer topK = ObjectUtil.defaultIfNull(request.getTopK(), DEFAULT_TOP_K);
        Double threshold = ObjectUtil.defaultIfNull(request.getSimilarityThreshold(), DEFAULT_SIMILARITY_THRESHOLD);
        return doChatStream(chatModel, embeddingModel, request.getQuestion(), topK, threshold);
    }

    /**
     * 用户端流式问答：用默认启用的聊天模型和向量模型，走 SSE 流式返回
     */
    public SseEmitter frontChatStream(KnowledgeChatRequest request) {
        if (request == null || StrUtil.isBlank(request.getQuestion())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请输入问题");
        }
        AiModelConfig chatModel = getDefaultModelConfig("聊天模型");
        AiModelConfig embeddingModel = getDefaultModelConfig("向量模型");
        return doChatStream(chatModel, embeddingModel, request.getQuestion(), DEFAULT_TOP_K, DEFAULT_SIMILARITY_THRESHOLD);
    }

    private SseEmitter doChatStream(AiModelConfig chatModel, AiModelConfig embeddingModel,
                                    String question, Integer topK, Double threshold) {
        SseEmitter emitter = new SseEmitter(120000L);
        // 检索和模型调用都放到异步线程，避免阻塞请求线程，异常统一通过 error 事件下发
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 问题向量化 + 检索相似片段 + 补文档标题
                List<Double> questionVector = embeddingService.embed(embeddingModel, question);
                List<KnowledgeSource> sources = pgVectorService.searchSimilar(questionVector, topK, threshold);
                fillDocumentTitle(sources);
                // 2. 先把资料来源发给前端，右侧来源区可以立即展示
                sendSse(emitter, "sources", sources);
                // 3. 没有命中资料就不调模型，直接下发兜底话术
                if (sources.isEmpty()) {
                    sendSse(emitter, "message", "知识库中没有检索到足够相关的资料，建议补充知识文档或降低相似度阈值后再试。");
                    sendSse(emitter, "done", "");
                    emitter.complete();
                    return;
                }
                // 4. 命中资料后流式调用聊天模型，每个增量片段通过 message 事件下发
                callChatModelStream(chatModel, question, sources, content -> sendSse(emitter, "message", content));
                sendSse(emitter, "done", "");
                emitter.complete();
            } catch (Exception e) {
                sendSse(emitter, "error", e.getMessage());
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private void sendSse(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "流式响应发送失败：" + e.getMessage());
        }
    }

    private void fillDocumentTitle(List<KnowledgeSource> sources) {
        for (KnowledgeSource source : sources) {
            if (source.getDocumentId() == null) {
                continue;
            }
            KnowledgeDocument document = knowledgeDocumentMapper.selectById(source.getDocumentId());
            if (document != null) {
                source.setDocumentTitle(document.getTitle());
            }
        }
    }

    private AiModelConfig getModelConfig(Integer id, String modelType) {
        if (id == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请选择" + modelType + "配置");
        }
        AiModelConfig modelConfig = aiModelConfigMapper.selectById(id);
        if (modelConfig == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, modelType + "配置不存在");
        }
        if (!modelType.equals(modelConfig.getModelType())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请选择正确的" + modelType + "配置");
        }
        if (!"是".equals(modelConfig.getEnabled())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, modelType + "配置未启用");
        }
        if (StrUtil.hasBlank(modelConfig.getBaseUrl(), modelConfig.getApiKey(), modelConfig.getModelName())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, modelType + "配置不完整");
        }
        if (modelConfig.getApiKey().contains("请替换")) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请先在模型配置中填写真实 API Key");
        }
        return modelConfig;
    }

    private AiModelConfig getDefaultModelConfig(String modelType) {
        AiModelConfig query = new AiModelConfig();
        query.setModelType(modelType);
        query.setEnabled("是");
        List<AiModelConfig> list = aiModelConfigMapper.selectAll(query);
        if (list.isEmpty()) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请先配置启用的" + modelType);
        }
        return list.get(0);
    }

    private void callChatModelStream(AiModelConfig chatModel, String question,
                                     List<KnowledgeSource> sources, Consumer<String> contentConsumer) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", chatModel.getModelName());
            requestBody.set("temperature", 0.2);
            // stream=true 让上游按 SSE 逐段返回
            requestBody.set("stream", true);
            requestBody.set("messages", buildMessages(question, sources));

            // 用 JDK 原生 HttpClient 读流
            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(buildChatUrl(chatModel.getBaseUrl())))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + chatModel.getApiKey())
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                    .build();

            java.net.http.HttpResponse<java.io.InputStream> response = HttpClient.newHttpClient()
                    .send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                    throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "聊天模型调用失败：" + reader.readLine());
                }
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 上游 SSE 每行形如 data: {...}，只处理 data 行
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    // [DONE] 表示上游流结束
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    if (StrUtil.isBlank(data)) {
                        continue;
                    }
                    // 从 choices[0].delta.content 取增量内容
                    JSONObject dataNode = JSONUtil.parseObj(data);
                    JSONArray choices = dataNode.getJSONArray("choices");
                    if (choices == null || choices.isEmpty()) {
                        continue;
                    }
                    JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                    String content = delta == null ? null : delta.getStr("content");
                    if (StrUtil.isNotBlank(content)) {
                        contentConsumer.accept(content);
                    }
                }
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "聊天模型流式调用异常：" + e.getMessage());
        }
    }

    private JSONArray buildMessages(String question, List<KnowledgeSource> sources) {
        JSONArray messages = new JSONArray();

        JSONObject system = new JSONObject();
        system.set("role", "system");
        system.set("content", "你是企业智能客服助手。请严格基于给定资料回答问题；资料中没有的信息不要编造。回答要清晰、准确、适合客服场景。");
        messages.add(system);

        JSONObject user = new JSONObject();
        user.set("role", "user");
        user.set("content", buildUserPrompt(question, sources));
        messages.add(user);
        return messages;
    }

    private String buildUserPrompt(String question, List<KnowledgeSource> sources) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：\n").append(question).append("\n\n");
        builder.append("企业资料：\n");
        for (int i = 0; i < sources.size(); i++) {
            KnowledgeSource source = sources.get(i);
            builder.append("【资料").append(i + 1).append("，相似度")
                    .append(String.format("%.2f", source.getSimilarity())).append("】\n")
                    .append(source.getContent()).append("\n\n");
        }
        builder.append("请根据企业资料回答用户问题。如果资料不足，请明确说明资料不足。");
        return builder.toString();
    }

    private String buildChatUrl(String baseUrl) {
        String value = StrUtil.removeSuffix(baseUrl.trim(), "/");
        if (value.endsWith("/chat/completions")) {
            return value;
        }
        return value + "/chat/completions";
    }
}
