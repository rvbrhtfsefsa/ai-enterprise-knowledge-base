package com.example.entity;

public class KnowledgeChatRequest {

    /** 用户问题 */
    private String question;
    /** 聊天模型配置ID */
    private Integer chatModelConfigId;
    /** 向量模型配置ID */
    private Integer embeddingModelConfigId;
    /** 召回片段数量 */
    private Integer topK;
    /** 相似度阈值 */
    private Double similarityThreshold;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getChatModelConfigId() {
        return chatModelConfigId;
    }

    public void setChatModelConfigId(Integer chatModelConfigId) {
        this.chatModelConfigId = chatModelConfigId;
    }

    public Integer getEmbeddingModelConfigId() {
        return embeddingModelConfigId;
    }

    public void setEmbeddingModelConfigId(Integer embeddingModelConfigId) {
        this.embeddingModelConfigId = embeddingModelConfigId;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(Double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
}
