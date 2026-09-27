package com.example.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.config.PgVectorConfig;
import com.example.entity.AiModelConfig;
import com.example.exception.CustomException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    @Resource
    private PgVectorConfig pgVectorConfig;

    public List<Double> embed(AiModelConfig modelConfig, String text) {
        validateModelConfig(modelConfig);
        if (StrUtil.isBlank(text)) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "知识片段内容不能为空");
        }

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", modelConfig.getModelName());
        requestBody.set("input", text);
        requestBody.set("dimensions", pgVectorConfig.getDimension());

        String url = buildEmbeddingUrl(modelConfig.getBaseUrl());
        try (HttpResponse response = HttpRequest.post(url)
                .bearerAuth(modelConfig.getApiKey())
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(60000)
                .execute()) {
            if (!response.isOk()) {
                throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "向量模型调用失败：" + response.body());
            }
            return parseEmbedding(response.body());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "向量模型调用异常：" + e.getMessage());
        }
    }

    private void validateModelConfig(AiModelConfig modelConfig) {
        if (modelConfig == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请选择向量模型配置");
        }
        if (!"向量模型".equals(modelConfig.getModelType())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请选择向量模型类型的配置");
        }
        if (!"是".equals(modelConfig.getEnabled())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "当前向量模型配置未启用");
        }
        if (StrUtil.hasBlank(modelConfig.getBaseUrl(), modelConfig.getApiKey(), modelConfig.getModelName())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "向量模型配置不完整");
        }
        if (modelConfig.getApiKey().contains("请替换")) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "请先在模型配置中填写真实 API Key");
        }
    }

    private String buildEmbeddingUrl(String baseUrl) {
        String value = StrUtil.removeSuffix(baseUrl.trim(), "/");
        if (value.endsWith("/embeddings")) {
            return value;
        }
        return value + "/embeddings";
    }

    private List<Double> parseEmbedding(String responseBody) {
        JSONObject result = JSONUtil.parseObj(responseBody);
        JSONArray data = result.getJSONArray("data");
        if (data == null || data.isEmpty()) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "向量模型返回结果为空");
        }
        JSONObject first = data.getJSONObject(0);
        JSONArray embedding = first.getJSONArray("embedding");
        if (embedding == null || embedding.isEmpty()) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "向量模型没有返回 embedding");
        }

        List<Double> vector = new ArrayList<>();
        for (Object item : embedding) {
            vector.add(Double.parseDouble(String.valueOf(item)));
        }
        if (!vector.isEmpty() && vector.size() != pgVectorConfig.getDimension()) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "向量维度不匹配，当前返回维度：" + vector.size());
        }
        return vector;
    }
}
